package no.nav.foreldrepenger.abakus.jetty;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;

public class JettyDevServer extends JettyServer {

    private static final Environment ENV = Environment.current();
    private static final Logger LOG = LoggerFactory.getLogger(JettyDevServer.class);

    private JettyDevServer(int serverPort) {
        super(serverPort);
    }

    public static void main(String[] args) throws Exception {
        initTrustStoreAndKeyStore();
        jettyServer(args).bootStrap();
    }

    static JettyDevServer jettyServer(String[] args) {
        if (args.length > 0) {
            return new JettyDevServer(Integer.parseUnsignedInt(args[0]));
        }
        return new JettyDevServer(ENV.getProperty("server.port", Integer.class, 8015));
    }

    private static void initTrustStoreAndKeyStore() {
        var keystoreRelativPath = ENV.getProperty("keystore.relativ.path");
        var truststoreRelativPath = ENV.getProperty("truststore.relativ.path");
        var keystoreTruststorePassword = ENV.getProperty("vtp.ssl.passord");
        var absolutePathHome = ENV.getProperty("user.home", ".");
        System.setProperty("javax.net.ssl.trustStore", absolutePathHome + truststoreRelativPath);
        System.setProperty("javax.net.ssl.keyStore", absolutePathHome + keystoreRelativPath);
        System.setProperty("javax.net.ssl.trustStorePassword", keystoreTruststorePassword);
        System.setProperty("javax.net.ssl.keyStorePassword", keystoreTruststorePassword);
        System.setProperty("javax.net.ssl.password", keystoreTruststorePassword);
        // KAFKA spesifikke properties
        System.setProperty("KAFKA_TRUSTSTORE_PATH", absolutePathHome + truststoreRelativPath);
        System.setProperty("KAFKA_KEYSTORE_PATH", absolutePathHome + keystoreRelativPath);
        System.setProperty("KAFKA_CREDSTORE_PASSWORD", keystoreTruststorePassword);
    }

    @Override
    void migrerDatabaser() {
        try {
            super.migrerDatabaser();
        } catch (Exception e) {
            LOG.info("Migreringer feilet, cleaner og prøver på nytt for lokal db.");
            try (var migreringDs = DatasourceUtil.createDatasource(DatasourceRole.ADMIN, 2)) {
                var flyway = Flyway.configure()
                    .dataSource(migreringDs)
                    .locations("classpath:/db/migration/")
                    .baselineOnMigrate(true)
                    .cleanDisabled(false)
                    .load();
                flyway.clean();
            } catch (FlywayException fwe) {
                throw new IllegalStateException("Migrering feiler.", fwe);
            }
            super.migrerDatabaser();
        }
    }
}
