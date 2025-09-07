package no.nav.foreldrepenger.abakus.jetty;

import java.io.File;

import javax.naming.NamingException;

import org.eclipse.jetty.ee11.cdi.CdiDecoratingListener;
import org.eclipse.jetty.ee11.cdi.CdiServletContainerInitializer;
import org.eclipse.jetty.ee11.servlet.DefaultServlet;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.ee11.servlet.security.ConstraintMapping;
import org.eclipse.jetty.ee11.servlet.security.ConstraintSecurityHandler;
import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.security.Constraint;
import org.eclipse.jetty.server.Server;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import no.nav.foreldrepenger.abakus.app.konfig.ApiConfig;
import no.nav.foreldrepenger.abakus.app.konfig.EksternApiConfig;
import no.nav.foreldrepenger.abakus.app.konfig.ForvaltningApiConfig;
import no.nav.foreldrepenger.abakus.app.konfig.InternalApiConfig;
import no.nav.foreldrepenger.abakus.app.tjenester.ServiceStarterListener;
import no.nav.foreldrepenger.konfig.Environment;

public class JettyServer {

    private static final Logger LOG = LoggerFactory.getLogger(JettyServer.class);
    private static final Environment ENV = Environment.current();
    protected static final String APPLICATION = "jakarta.ws.rs.Application";

    private static final String CONTEXT_PATH = ENV.getProperty("context.path", "/fpabakus");

    private final Integer serverPort;

    JettyServer(int serverPort) {
        this.serverPort = serverPort;
    }

    public static void main(String[] args) throws Exception {
        jettyServer(args).bootStrap();
    }

    private static JettyServer jettyServer(String[] args) {
        if (args.length > 0) {
            return new JettyServer(Integer.parseUnsignedInt(args[0]));
        }
        return new JettyServer(ENV.getProperty("server.port", Integer.class, 8080));
    }

    void bootStrap() throws Exception {
        konfigurerSikkerhet();
        konfigurerJndi();
        konfigurerLogging();
        migrerDatabaser();
        start();
    }

    private static void initTrustStore() {
        final var trustStorePathProp = "javax.net.ssl.trustStore";
        final var trustStorePasswordProp = "javax.net.ssl.trustStorePassword";

        var defaultLocation = ENV.getProperty("user.home", ".") + "/.modig/truststore.jks";
        var storePath = ENV.getProperty(trustStorePathProp, defaultLocation);
        var storeFile = new File(storePath);
        if (!storeFile.exists()) {
            throw new IllegalStateException(
                "Finner ikke truststore i " + storePath + "\n\tKonfrigurer enten som System property '" + trustStorePathProp
                    + "' eller environment variabel '" + trustStorePathProp.toUpperCase().replace('.', '_') + "'");
        }
        var password = ENV.getProperty(trustStorePasswordProp, "changeit");
        System.setProperty(trustStorePathProp, storeFile.getAbsolutePath());
        System.setProperty(trustStorePasswordProp, password);
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
        var server = new Server(getServerPort());
        LOG.info("Starter server");
        var context = new ServletContextHandler(CONTEXT_PATH, ServletContextHandler.NO_SESSIONS);

        // Sikkerhet
        context.setSecurityHandler(simpleConstraints());

        // Servlets
        registerDefaultServlet(context);
        registerServlet(context, 0, InternalApiConfig.API_URI, InternalApiConfig.class);
        registerServlet(context, 1, ApiConfig.API_URI, ApiConfig.class);
        registerServlet(context, 2, ForvaltningApiConfig.API_URI, ForvaltningApiConfig.class);
        registerServlet(context, 3, EksternApiConfig.API_URI, EksternApiConfig.class);

        // Starter tjenester
        context.addEventListener(new ServiceStarterListener());

        // Enable Weld + CDI
        context.setInitParameter(CdiServletContainerInitializer.CDI_INTEGRATION_ATTRIBUTE, CdiDecoratingListener.MODE);
        context.addServletContainerInitializer(new CdiServletContainerInitializer());
        context.addServletContainerInitializer(new org.jboss.weld.environment.servlet.EnhancedListener());

        server.setHandler(context);
        server.setStopAtShutdown(true);
        server.setStopTimeout(10000);
        server.start();

        LOG.info("Server startet på port: {}", getServerPort());
        server.join();
    }

    private static void registerDefaultServlet(ServletContextHandler context) {
        var defaultServlet = new ServletHolder(new DefaultServlet());
        context.addServlet(defaultServlet, "/*");
    }

    private static void registerServlet(ServletContextHandler context, int prioritet, String path, Class<?> appClass) {
        var servlet = new ServletHolder(new ServletContainer());
        servlet.setInitOrder(prioritet);
        servlet.setInitParameter(APPLICATION, appClass.getName());
        context.addServlet(servlet, path + "/*");
    }

    private void konfigurerSikkerhet() {
        if (ENV.isLocal()) {
            initTrustStore();
        }
    }

    protected void konfigurerJndi() throws NamingException {
        // Balanser så CP-size = TaskThreads+1 + Antall Connections man ønsker
        System.setProperty("task.manager.runner.threads", "6");
        new EnvEntry("jdbc/defaultDS", DatasourceUtil.createDatasource(DatasourceRole.USER, 12));
    }

    void migrerDatabaser() {
        try (var dataSource = DatasourceUtil.createDatasource(DatasourceRole.ADMIN, 2)) {
            var flyway = Flyway.configure().dataSource(dataSource).locations("classpath:/db/migration/defaultDS").baselineOnMigrate(true);
            if (ENV.isProd() || ENV.isDev()) {
                flyway.initSql(String.format("SET ROLE \"%s\"", DatasourceUtil.getRole(DatasourceRole.ADMIN)));
            }
            flyway.load().migrate();
        } catch (FlywayException e) {
            LOG.error("Feil under migrering av databasen.");
            throw e;
        }
    }

    private static ConstraintSecurityHandler simpleConstraints() {
        var handler = new ConstraintSecurityHandler();
        // Slipp gjennom kall fra plattform til JaxRs. Foreløpig kun behov for GET
        handler.addConstraintMapping(pathConstraint(Constraint.ALLOWED, InternalApiConfig.API_URI + "/*"));
        // Slipp gjennom til autentisering i JaxRs / auth-filter
        handler.addConstraintMapping(pathConstraint(Constraint.ALLOWED, ApiConfig.API_URI + "/*"));
        // Slipp gjennom til autentisering i JaxRs / auth-filter
        handler.addConstraintMapping(pathConstraint(Constraint.ALLOWED, ForvaltningApiConfig.API_URI + "/*"));
        // Slipp gjennom til autentisering i JaxRs / auth-filter
        handler.addConstraintMapping(pathConstraint(Constraint.ALLOWED, EksternApiConfig.API_URI + "/*"));
        // Alt annet av paths og metoder forbudt - 403
        handler.addConstraintMapping(pathConstraint(Constraint.FORBIDDEN, "/*"));
        return handler;
    }

    private static ConstraintMapping pathConstraint(Constraint constraint, String path) {
        var mapping = new ConstraintMapping();
        mapping.setConstraint(constraint);
        mapping.setPathSpec(path);
        return mapping;
    }

    private Integer getServerPort() {
        return this.serverPort;
    }

}
