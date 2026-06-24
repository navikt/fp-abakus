package no.nav.foreldrepenger.abakus.jetty;

import java.util.Locale;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.callback.BaseCallback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import no.nav.foreldrepenger.abakus.app.konfig.ApiConfig;
import no.nav.foreldrepenger.abakus.app.konfig.EksternApiConfig;
import no.nav.foreldrepenger.abakus.app.konfig.ForvaltningApiConfig;
import no.nav.foreldrepenger.abakus.app.konfig.InternalApiConfig;
import no.nav.foreldrepenger.abakus.app.tjenester.ServiceStarterListener;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.felles.jpa.NamingStandard;
import no.nav.vedtak.felles.jpa.flyway.FlywayUtil;
import no.nav.vedtak.felles.jpa.jdbc.DataSourceHolder;
import no.nav.vedtak.log.metrics.MetricsUtil;
import no.nav.vedtak.server.jetty.DataSourceShutdownListener;
import no.nav.vedtak.server.jetty.JettyServerBuilder;

public class JettyServer {

    private static final Logger LOG = LoggerFactory.getLogger(JettyServer.class);
    private static final Environment ENV = Environment.current();

    private static final String CONTEXT_PATH = ENV.getProperty("context.path", "/fpabakus");

    private final Integer serverPort;

    JettyServer(int serverPort) {
        this.serverPort = serverPort;
    }

    static void main() throws Exception {
        LOG.info("JVM Default Locale: {}", Locale.getDefault());
        jettyServer().bootStrap();
    }

    protected static JettyServer jettyServer() {
        return new JettyServer(ENV.getProperty("server.port", Integer.class, 8080));
    }

    void bootStrap() throws Exception {
        MetricsUtil.init();
        konfigurerLogging();
        migrerDatabaser();
        konfigurerDataSource();
        start();
    }

    /**
     * Vi bruker SLF4J + logback, Jersey brukes JUL for logging.
     * Setter opp en bridge til å få Jersey til å logge gjennom Logback også.
     */
    private static void konfigurerLogging() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private void start() throws Exception{
        LOG.info("Starter server");
        var server = JettyServerBuilder.builder()
            .port(getServerPort())
            .contextPath(CONTEXT_PATH)
            .addEventListener(new ServiceStarterListener())
            .addEventListener(new DataSourceShutdownListener(DataSourceHolder::close))
            .registerRestApp(InternalApiConfig.API_URI, InternalApiConfig.class)
            .registerRestApp(ApiConfig.API_URI, ApiConfig.class)
            .registerRestApp(ForvaltningApiConfig.API_URI, ForvaltningApiConfig.class)
            .registerRestApp(EksternApiConfig.API_URI, EksternApiConfig.class)
            .build();
        server.start();
        LOG.info("Server startet på port: {}", getServerPort());
        server.join();
    }

    protected void konfigurerDataSource() {
        // Balanser så CP-size = TaskThreads+1 + Antall Connections man ønsker
        System.setProperty("task.manager.runner.threads", "6");
        var dataSource = LocalDatasourceUtil.createDatasource(18);
        DataSourceHolder.initialize(dataSource);
    }

    void migrerDatabaser() {
        // Spesielt oppsett Postgres On-prem pga roles og callbacks
        try (var dataSource = LocalDatasourceUtil.createMigrationDatasource()) {
            var flyway = FlywayUtil.flywayConfig(dataSource, NamingStandard.DEFAULT_DS_MIGRATION_CLASSPATH);
            if (ENV.isProd() || ENV.isDev()) {
                flyway.callbacks(new BaseCallback() {
                    @Override
                    public boolean supports(Event event, Context context) {
                        return event == Event.AFTER_CONNECT;
                    }

                    @Override
                    public void handle(Event event, Context context) {
                        try (var stmt = context.getConnection().createStatement()) {
                            stmt.execute(String.format("SET ROLE \"%s\"", LocalDatasourceUtil.getRole(DatasourceRole.ADMIN))); // NOSONAR
                        } catch (Exception e) {
                            throw new FlywayException("Kunne ikke sette rolle etter connect", e);
                        }
                    }
                });
            }
            flyway.load().migrate();
        }
    }

    private Integer getServerPort() {
        return this.serverPort;
    }

}
