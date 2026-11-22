package net.guarnie.maven.plugin.data.jdbc.postgres;

import org.flywaydb.core.Flyway;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * PostgreSQL server shared by all the integration tests of the module ("singleton container").
 * <p>
 * It starts on first use of the class and is stopped by Testcontainers (Ryuk) at the end of the JVM.
 * The schema and the sample data are migrated once, before any test runs.
 */
public final class PostgresTestServer {
    private static final String MIGRATIONS_LOCATION = "classpath:db/migration/postgresql";

    private static final PostgreSQLContainer CONTAINER = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("secret");

    static {
        CONTAINER.start();
        Flyway.configure()
                .dataSource(CONTAINER.getJdbcUrl(), CONTAINER.getUsername(), CONTAINER.getPassword())
                .locations(MIGRATIONS_LOCATION)
                .load()
                .migrate();
    }

    private PostgresTestServer() {}

    public static JdbcDatabaseContainer<?> container() {
        return CONTAINER;
    }
}
