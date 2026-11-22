package net.guarnie.maven.plugin.data.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Common base of the integration tests: provides the database connection file and a clean output
 * folder for every test. The database itself (a singleton container) is provided by the subclasses.
 */
public abstract class AbstractIntegrationTest {
    private static final String GENERATED_SOURCES = "generated-sources";
    private static final String MAPPINGS_FILE = "mappings.yaml";
    protected static final String GENERATED_PACKAGE_NAME = "net.guarnie.generated";
    protected Path tempDir;
    protected Path envFile;
    protected Path outputDir;

    protected abstract JdbcDatabaseContainer<?> getContainer();

    protected String getSchemaName() {
        return "public";
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

    /**
     * Runs the generator on the test database, using the given mappings.yml content.
     *
     * @param mappingsYaml content of the mappings file
     * @throws Exception if the generation fails
     */
    protected void executeMojo(String mappingsYaml) throws Exception {
        Path mappingFile = tempDir.resolve(MAPPINGS_FILE);
        Files.writeString(mappingFile, mappingsYaml);

        GeneratorMojo mojo = new GeneratorMojo();
        mojo.setEnvPath(envFile);
        mojo.setPackageName(GENERATED_PACKAGE_NAME);
        mojo.setOutputPath(outputDir);
        mojo.setMappingsPath(mappingFile);

        mojo.execute();
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
