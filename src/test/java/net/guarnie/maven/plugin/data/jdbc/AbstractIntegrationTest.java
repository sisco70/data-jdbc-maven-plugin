package net.guarnie.maven.plugin.data.jdbc;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractIntegrationTest {
    private static final String GENERATED_SOURCES = "generated-sources";
    protected static final String GENERATED_PACKAGE_NAME = "net.guarnie.generated";
    protected Path tempDir;
    protected Path envFile;
    protected Path outputDir;

    protected abstract JdbcDatabaseContainer<?> getContainer();

    protected abstract String getDbVendor();

    protected String getSchemaName() {
        return "public";
    }

    @BeforeAll
    void migrateDatabase() {
        JdbcDatabaseContainer<?> container = getContainer();
        String vendorLocation = "classpath:db/migration/" + getDbVendor();

        Flyway.configure()
                .dataSource(container.getJdbcUrl(), container.getUsername(), container.getPassword())
                .locations(vendorLocation)
                .load()
                .migrate();
    }

    private Path createEnvFile(Path tempDir) throws Exception {
        JdbcDatabaseContainer<?> container = getContainer();
        Path envFile = tempDir.resolve(".env");
        List<String> envLines = List.of(
                "jdbc.driver=" + container.getDriverClassName(),
                "jdbc.url=" + container.getJdbcUrl(),
                "jdbc.schema=" + getSchemaName(),
                "jdbc.user=" + container.getUsername(),
                "jdbc.pass=" + container.getPassword()
        );
        Files.write(envFile, envLines);
        return envFile;
    }

    @BeforeEach
    void setUpEnvironment(@TempDir Path tempDir) throws Exception {
        this.tempDir = tempDir;
        envFile = createEnvFile(tempDir);
        outputDir = tempDir.resolve(GENERATED_SOURCES);
    }

    protected void executeRecordQueryTest(
            String className,
            String sql,
            List<Object> params,
            QueryHelper.TableTestExecutor executor
    ) throws Exception {
        QueryHelper.execQuery(getContainer(), outputDir, GENERATED_PACKAGE_NAME, className, sql, params, executor);
    }
}