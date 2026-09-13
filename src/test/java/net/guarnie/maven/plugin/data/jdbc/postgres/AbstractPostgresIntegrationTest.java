package net.guarnie.maven.plugin.data.jdbc.postgres;

import net.guarnie.maven.plugin.data.jdbc.AbstractIntegrationTest;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Container;


public abstract class AbstractPostgresIntegrationTest extends AbstractIntegrationTest {
    @Container
    protected static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("secret");

    @Override
    protected JdbcDatabaseContainer<?> getContainer() {
        return POSTGRES;
    }

    @Override
    protected String getDbVendor() {
        return "postgresql";
    }
}