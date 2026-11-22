package net.guarnie.maven.plugin.data.jdbc.postgres;

import net.guarnie.maven.plugin.data.jdbc.AbstractIntegrationTest;
import org.testcontainers.containers.JdbcDatabaseContainer;

/**
 * Common base of the PostgreSQL integration tests: all subclasses share the same server.
 */
public abstract class AbstractPostgresIntegrationTest extends AbstractIntegrationTest {

    @Override
    protected JdbcDatabaseContainer<?> getContainer() {
        return PostgresTestServer.container();
    }
}
