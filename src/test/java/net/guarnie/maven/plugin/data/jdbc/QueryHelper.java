package net.guarnie.maven.plugin.data.jdbc;

import org.junit.jupiter.api.Assertions;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;

public class QueryHelper {
    private static final DefaultConversionService CONVERSION_SERVICE = createGenericConversionService();

    private QueryHelper() {}

    @FunctionalInterface
    public interface TableTestExecutor {
        void execute(List<?> results, Class<?> recordClass) throws Exception;
    }


    @SuppressWarnings("unchecked")
    public static <T> T get(Object record, String fieldName) {
        try {
            Method method = record.getClass().getMethod(fieldName);
            return (T) method.invoke(record);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke accessor '" + fieldName + "' on record", e);
        }
    }

    private static DefaultConversionService createGenericConversionService() {
        DefaultConversionService conversionService = new DefaultConversionService();
        ObjectMapper objectMapper = new ObjectMapper();

        conversionService.addConverter(String.class, JsonNode.class, source -> {
            if (source.isBlank()) return null;
            try {
                return objectMapper.readTree(source);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to parse JSON String to JsonNode", e);
            }
        });

        try {
            Class<?> pgObjectClass = Class.forName("org.postgresql.util.PGobject");
            Method getValueMethod = pgObjectClass.getMethod("getValue");

            conversionService.addConverter(pgObjectClass, JsonNode.class, source -> {
                try {
                    String jsonStr = (String) getValueMethod.invoke(source);
                    return jsonStr != null ? objectMapper.readTree(jsonStr) : null;
                } catch (Exception e) {
                    throw new IllegalArgumentException("Failed to parse PGobject to JsonNode", e);
                }
            });
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {}

        return conversionService;
    }

    public static void execQuery(
            JdbcDatabaseContainer<?> container,
            Path outputDir,
            String packageName,
            String className,
            String sql,
            List<Object> params,
            TableTestExecutor testExecutor
    ) throws Exception {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

        try (URLClassLoader classLoader = new URLClassLoader(
                new URL[]{ outputDir.toUri().toURL() },
                QueryHelper.class.getClassLoader()
        )) {
            String fullClassName = packageName + "." + className;
            Class<?> recordClass = Class.forName(fullClassName, true, classLoader);
            Assertions.assertTrue(recordClass.isRecord(), "The generated class must be a Record!");

            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(container.getDriverClassName());
            dataSource.setUrl(container.getJdbcUrl());
            dataSource.setUsername(container.getUsername());
            dataSource.setPassword(container.getPassword());

            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            Thread.currentThread().setContextClassLoader(classLoader);

            DataClassRowMapper<?> rowMapper = DataClassRowMapper.newInstance(recordClass);
            rowMapper.setConversionService(CONVERSION_SERVICE);

            List<?> results = jdbcTemplate.query(sql, rowMapper, params.toArray());

            testExecutor.execute(results, recordClass);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }
}