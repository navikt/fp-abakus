package no.nav.foreldrepenger.abakus.app.selftest.checks;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@ApplicationScoped
public class DatabaseHealthCheck {

    private static final String JDBC_DEFAULT_DS = "jdbc/defaultDS";
    private static final String SQL_QUERY = "select count(1) from prosess_task_type";
    private String jndiName;
    // må være rask, og bruke et stabilt tabell-navn
    private String endpoint = null; // ukjent frem til første gangs test

    DatabaseHealthCheck() {
        this.jndiName = JDBC_DEFAULT_DS;
    }

    DatabaseHealthCheck(String dsJndiName) {
        this.jndiName = dsJndiName;
    }

    public boolean isReady() {

        DataSource dataSource;
        try {
            dataSource = (DataSource) new InitialContext().lookup(jndiName);
        } catch (NamingException e) {
            return false;
        }

        try (Connection connection = dataSource.getConnection()) {
            if (endpoint == null) {
                endpoint = extractEndpoint(connection);
            }
            try (Statement statement = connection.createStatement()) {
                if (!statement.execute(SQL_QUERY)) {
                    throw new SQLException("SQL-spørring ga ikke et resultatsett");
                }
            }
        } catch (SQLException e) {
            return false;
        }

        return true;
    }

    private String extractEndpoint(Connection connection) {
        String result = "?";
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String url = metaData.getURL();
            if (url != null) {
                if (!url.toUpperCase(Locale.US).contains("SERVICE_NAME=")) { // don't care about Norwegian letters here
                    url = url + "/" + connection.getSchema();
                }
                result = url;
            }
        } catch (SQLException e) { //NOSONAR
            // ikke fatalt
        }
        return result;
    }
}
