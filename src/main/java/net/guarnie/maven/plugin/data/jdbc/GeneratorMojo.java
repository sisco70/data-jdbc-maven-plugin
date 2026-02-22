package net.guarnie.maven.plugin.data.jdbc;


import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.internal.lang3.tuple.Pair;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

/**
 * Plugin maven per generare records per Spring Data JDBC.
 */
@Mojo(name = "generate-records", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class GeneratorMojo extends AbstractMojo {
    private static final Logger log = LoggerFactory.getLogger(GeneratorMojo.class);

    private static final String OFFSETDATETIME_CLASS = "java.time.OffsetDateTime";
    private static final String INSTANT_CLASS = "java.time.Instant";
    private static final String HBS_EXTENSION = ".hbs";
    private static final String TPL_TABLE_RECORD = "table-record";
    private static final String JDBC_URL = "jdbc.url";
    private static final String JDBC_USER = "jdbc.user";
    private static final String JDBC_PASS = "jdbc.pass";
    private static final String JDBC_SCHEMA = "jdbc.schema";


    @Parameter(defaultValue = "${project.basedir}/.env")
    private Path envPath;

    @Parameter
    private Path mappingsPath;

    @Parameter(required = true)
    private String packageName;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/jdbc-records")
    private Path outputPath;

    @Parameter(defaultValue = "false")
    private boolean useOffsetDateTime;

    @Parameter
    private Path templatesPath;

    private GeneratorMappings mappings;
    private Template template;
    private String timestampTzClassName;


    @Override
    public void execute() throws MojoExecutionException {
        log.info("Starting Spring Data JDBC Record Generation");

        this.timestampTzClassName = useOffsetDateTime? OFFSETDATETIME_CLASS : INSTANT_CLASS;
        this.mappings = loadMappings(mappingsPath);
        Properties dbEnv = loadEnv(envPath);
        this.template = loadTemplate(templatesPath);

        try (Connection conn = DriverManager.getConnection(dbEnv.getProperty(JDBC_URL), dbEnv.getProperty(JDBC_USER), dbEnv.getProperty(JDBC_PASS))) {
            DatabaseMetaData meta = conn.getMetaData();
            String schema = dbEnv.getProperty(JDBC_SCHEMA);

            try (ResultSet rsTables = meta.getTables(null, schema, "%", new String[]{"TABLE"})) {
                while (rsTables.next()) {
                    String tableName = rsTables.getString("TABLE_NAME");
                    if (mappings.shouldProcessTable(tableName)) generateRecordFile(meta, tableName, schema);
                }
            }
        } catch (Exception e) {
            log.error("Critical error during record generation", e);
            throw new MojoExecutionException("Generation failed", e);
        }
    }

    /**
     * Genera un file java che rappresenta una classe java record che mappa
     * una tabella di un certo schema appartenente a un database.
     * @param meta Metadata db
     * @param tableName Nome tabella
     * @param schema Schema dn
     * @throws Exception Eccezione durante l'accesso al db
     */
    private void generateRecordFile(DatabaseMetaData meta, String tableName, String schema) throws Exception {
        String javaClassName = mappings.getMappedTableName(tableName);
        log.info("Generating: {} -> {}", tableName, javaClassName);

        List<Map<String, Object>> cols = new ArrayList<>();
        List<Map<String, Object>> pkCols = new ArrayList<>();
        Set<String> imports = new TreeSet<>();
        Set<String> pkNames = new HashSet<>();

        try (ResultSet rs = meta.getPrimaryKeys(null, schema, tableName)) {
            while (rs.next()) pkNames.add(rs.getString("COLUMN_NAME").toLowerCase());
        }

        try (ResultSet rs = meta.getColumns(null, schema, tableName, null)) {
            while (rs.next()) {
                String dbColName = rs.getString("COLUMN_NAME");
                int dataType = rs.getInt("DATA_TYPE");
                String typeName = rs.getString("TYPE_NAME");
                int precision = rs.getInt("COLUMN_SIZE");
                int scale = rs.getInt("DECIMAL_DIGITS");
                String fullType = mapSqlType(dataType, typeName, precision, scale);
                int dotPos = fullType.lastIndexOf(".");
                if (dotPos > -1 && !fullType.startsWith("java.lang.")) imports.add(fullType);

                Pair<String,Boolean> javaCol = mappings.getMappedColumnName(tableName, dbColName);
                String simpleType = fullType.substring(dotPos + 1);

                Map<String, Object> col = Map.of(
                        "javaName", javaCol.getLeft(),
                        "dbName", dbColName,
                        "type", simpleType,
                        "hasCustomMapping", javaCol.getRight()
                );

                if (pkNames.contains(dbColName.toLowerCase())) pkCols.add(col);
                else cols.add(col);
            }
        }

        // Preparo context per template Handlebars
        Map<String, Object> context = Map.of(
                "packageName", packageName,
                "className", javaClassName,
                "dbTableName", tableName,
                "hasCustomTableMapping", !tableName.equalsIgnoreCase(javaClassName),
                "pkColumns", pkCols,
                "columns", cols,
                "hasCompositePk", pkCols.size() > 1,
                "imports", imports
        );

        Path outDir = outputPath.resolve(packageName.replace(".", "/"));
        Files.createDirectories(outDir);
        Files.writeString(outDir.resolve(javaClassName + ".java"), template.apply(context));
    }

    /**
     * Ritorna il tipo java che mappa il campo su Database
     * @param type Codice tipo JDBC
     * @param typeName Tipo nome campo
     * @param precision Precisione del campo
     * @param scale Scala
     * @return Tipo java
     */
    private String mapSqlType(int type, String typeName, int precision, int scale) {
        String name = (typeName == null) ? "" : typeName.toLowerCase();

        return switch (type) {
            case Types.INTEGER, Types.SMALLINT, Types.TINYINT -> "java.lang.Integer";
            case Types.BIGINT -> "java.lang.Long";

            case Types.DECIMAL, Types.NUMERIC -> (scale <= 0)?  // Se la scala è 0 o -127 (Oracle), è un numero intero
                    ((precision > 0 && precision < 10)? "java.lang.Integer" : "java.lang.Long") : "java.math.BigDecimal";

            case Types.FLOAT, Types.REAL, Types.DOUBLE -> "java.lang.Double";
            case Types.VARCHAR, Types.CHAR, Types.LONGVARCHAR, Types.CLOB, Types.NVARCHAR, Types.NCHAR -> "java.lang.String";
            case Types.BOOLEAN, Types.BIT -> "java.lang.Boolean";

            // DATE E TEMPO: Supporto SQL Server (-155) e Postgres
            case -155, Types.TIMESTAMP_WITH_TIMEZONE -> timestampTzClassName;
            case Types.TIMESTAMP -> name.contains("tz") || name.contains("offset")? timestampTzClassName : "java.time.LocalDateTime";
            case Types.DATE -> "java.time.LocalDate";
            case Types.TIME -> "java.time.LocalTime";
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY, Types.BLOB -> "byte[]";

            // TIPI SPECIALI (Postgres "OTHER", Oracle "RAW", ecc.)
            case Types.OTHER, Types.ROWID -> switch (name) {
                case "jsonb", "json" -> "com.fasterxml.jackson.databind.JsonNode";
                case "uuid" -> "java.util.UUID";
                default -> "java.lang.String";
            };

            // FALLBACK
            default -> {
                if (name.contains("json")) yield "com.fasterxml.jackson.databind.JsonNode";
                log.warn("Unsupported SQL type: {} (name: {}), falling back to Object", type, name);
                yield "java.lang.Object";
            }
        };
    }

    private GeneratorMappings loadMappings(Path path) throws MojoExecutionException {
        if (path == null) {
            log.info("No mappings file specified. Falling back to default values.");
            return new GeneratorMappings();
        }
        else {
            if (!Files.exists(path)) throw new MojoExecutionException("Mappings path not exists: " + path);
            log.info("Using mappings file: {}", path);
            try (InputStream is = Files.newInputStream(path)) {
                Yaml yaml = new Yaml(new Constructor(GeneratorMappings.class, new LoaderOptions()));
                return yaml.load(is);
            } catch (Exception e) {
                throw new MojoExecutionException("Failed while parsing mappings file: ", e);
            }
        }
    }

    private Template loadTemplate(Path path) throws MojoExecutionException {
        TemplateLoader loader;
        if (path != null) {
            if (!Files.exists(path)) throw new MojoExecutionException("Templates path not exists: " + path);
            else loader = new FileTemplateLoader(path.toFile(), HBS_EXTENSION);
            log.info("Using template file from folder: {}", path);
        }
        else {
            log.info("No custom templates path specified. Falling back to default values.");
            loader = new ClassPathTemplateLoader("/templates",  HBS_EXTENSION);
        }

        try  {
            Handlebars handlebars = new Handlebars(loader);
            return handlebars.compile(TPL_TABLE_RECORD);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to parse record template: " + TPL_TABLE_RECORD, e);
        }
    }

    private Properties loadEnv(Path path) throws MojoExecutionException {
        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(path)) {
            props.load(is);
            return props;
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to load DB properties file: " + path, e);
        }
    }
}