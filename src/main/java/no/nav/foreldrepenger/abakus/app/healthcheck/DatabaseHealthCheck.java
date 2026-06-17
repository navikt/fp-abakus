package no.nav.foreldrepenger.abakus.app.healthcheck;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.vedtak.felles.jpa.jdbc.DataSourceHolder;
import no.nav.vedtak.server.LiveAndReadinessAware;

@ApplicationScoped
public class DatabaseHealthCheck implements LiveAndReadinessAware {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseHealthCheck.class);
    private static final String SQL_QUERY = "select 1";

    DatabaseHealthCheck() {
        // CDI
    }

    private boolean isOK() {
        if (!DataSourceHolder.isInitialized()) {
            return false;
        }
        try (var connection = DataSourceHolder.getDataSource().getConnection()) {
            try (var statement = connection.createStatement()) {
                if (!statement.execute(SQL_QUERY)) {
                    logWarning();
                    return false;
                }
            }
        } catch (SQLException _) {
            logWarning();
            return false;
        }

        return true;
    }

    private static void logWarning() {
        LOG.warn("Feil ved SQL-spørring {} mot databasen", SQL_QUERY);
    }

    @Override
    public boolean isReady() {
        return isOK();
    }

    @Override
    public boolean isAlive() {
        return isOK();
    }
}
