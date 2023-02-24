package no.nav.foreldrepenger.abakus.jetty;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.jetty.db.DatasourceRole;
import no.nav.foreldrepenger.abakus.jetty.db.DatasourceUtil;
import no.nav.foreldrepenger.konfig.Environment;

public class JettyDevServer extends JettyServer {

    private static final Environment ENV = Environment.current();
    private static final Logger LOG = LoggerFactory.getLogger(JettyDevServer.class);

    private JettyDevServer(int serverPort) {
        super(serverPort);
    }

    public static void main(String[] args) throws Exception {
        jettyServer(args).bootStrap();
    }

    static JettyDevServer jettyServer(String[] args) {
        if (args.length > 0) {
            return new JettyDevServer(Integer.parseUnsignedInt(args[0]));
        }
        return new JettyDevServer(ENV.getProperty("server.port", Integer.class, 8015));
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
