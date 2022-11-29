package no.nav.foreldrepenger.abakus.jetty;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.message.config.AuthConfigFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.jaas.JAASLoginService;
import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.jaspi.DefaultAuthConfigFactory;
import org.eclipse.jetty.security.jaspi.JaspiAuthenticatorFactory;
import org.eclipse.jetty.security.jaspi.provider.JaspiAuthConfigProvider;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.MetaData;
import org.eclipse.jetty.webapp.WebAppContext;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import no.nav.foreldrepenger.abakus.app.konfig.ApiConfig;
import no.nav.foreldrepenger.abakus.app.konfig.EksternApiConfig;
import no.nav.foreldrepenger.abakus.jetty.db.DatasourceRole;
import no.nav.foreldrepenger.abakus.jetty.db.DatasourceUtil;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.isso.IssoApplication;
import no.nav.vedtak.sikkerhet.ContextPathHolder;
import no.nav.vedtak.sikkerhet.jaspic.OidcAuthModule;

public class JettyServer {

    private static final Environment ENV = Environment.current();
    private static final Logger log = LoggerFactory.getLogger(JettyServer.class);

    private static final String CONTEXT_PATH = ENV.getProperty("context.path", "/fpabakus");
    private final Integer serverPort;

    JettyServer(int serverPort) {
        this.serverPort = serverPort;
        ContextPathHolder.instance(CONTEXT_PATH);
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

    private static void initTrustStore() {
        final String trustStorePathProp = "javax.net.ssl.trustStore";
        final String trustStorePasswordProp = "javax.net.ssl.trustStorePassword";

        var defaultLocation = ENV.getProperty("user.home", ".") + "/.modig/truststore.jks";
        var storePath = ENV.getProperty(trustStorePathProp, defaultLocation);
        var storeFile = new File(storePath);
        if (!storeFile.exists()) {
            throw new IllegalStateException("Finner ikke truststore i " + storePath
                + "\n\tKonfrigurer enten som System property '" + trustStorePathProp + "' eller environment variabel '"
                + trustStorePathProp.toUpperCase().replace('.', '_') + "'");
        }
        var password = ENV.getProperty(trustStorePasswordProp, "changeit");
        System.setProperty(trustStorePathProp, storeFile.getAbsolutePath());
        System.setProperty(trustStorePasswordProp, password);
    }

    private static WebAppContext createContext() throws IOException {
        var ctx = new WebAppContext();
        ctx.setParentLoaderPriority(true);

        // må hoppe litt bukk for å hente web.xml fra classpath i stedet for fra filsystem.
        String descriptor;
        try (var resource = Resource.newClassPathResource("/WEB-INF/web.xml")) {
            descriptor = resource.getURI().toURL().toExternalForm();
        }

        ctx.setDescriptor(descriptor);
        ctx.setContextPath(CONTEXT_PATH);
        ctx.setBaseResource(createResourceCollection());

        ctx.setInitParameter("pathInfoOnly", "true");
        ctx.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");

        /*
         * lar jetty scanne flere jars for web resources (eks. WebFilter/WebListener annotations),
         * men bare de som matchr pattern for raskere oppstart
         */
        ctx.setAttribute("org.eclipse.jetty.server.webapp.WebInfIncludeJarPattern",
            "^.*jersey-.*.jar$|^.*felles-sikkerhet-.*.jar$");
        ctx.setSecurityHandler(createSecurityHandler());

        updateMetaData(ctx.getMetaData());
        ctx.setThrowUnavailableOnStartupException(true);

        return ctx;
    }

    private static ResourceCollection createResourceCollection() {
        return new ResourceCollection(
            Resource.newClassPathResource("META-INF/resources/webjars/"),
            Resource.newClassPathResource("/web"));
    }

    private static HttpConfiguration createHttpConfiguration() {
        // Create HTTP Config
        var httpConfig = new HttpConfiguration();
        // Add support for X-Forwarded headers
        httpConfig.addCustomizer(new org.eclipse.jetty.server.ForwardedRequestCustomizer());
        return httpConfig;

    }

    private static SecurityHandler createSecurityHandler() {
        var securityHandler = new ConstraintSecurityHandler();
        securityHandler.setAuthenticatorFactory(new JaspiAuthenticatorFactory());

        var loginService = new JAASLoginService();
        loginService.setName("jetty-login");
        loginService.setLoginModuleName("jetty-login");
        loginService.setIdentityService(new DefaultIdentityService());
        securityHandler.setLoginService(loginService);
        return securityHandler;
    }

    private static void updateMetaData(MetaData metaData) {
        // Find path to class-files while starting jetty from development environment.
        var resources = getApplicationClasses().stream()
            .map(c -> Resource.newResource(c.getProtectionDomain().getCodeSource().getLocation()))
            .distinct()
            .toList();

        metaData.setWebInfClassesResources(resources);
    }

    private static List<Class<?>> getApplicationClasses() {
        return List.of(ApiConfig.class, EksternApiConfig.class, IssoApplication.class);
    }

    void bootStrap() throws Exception {
        konfigurerSikkerhet();
        konfigurerJndi();
        migrerDatabaser();
        start();
    }

    private void konfigurerSikkerhet() {
        if (ENV.isLocal()) {
            initTrustStore();
        }

        var factory = new DefaultAuthConfigFactory();
        factory.registerConfigProvider(new JaspiAuthConfigProvider(new OidcAuthModule()),
            "HttpServlet",
            "server " + CONTEXT_PATH,
            "OIDC Authentication");

        AuthConfigFactory.setFactory(factory);
    }

    protected void konfigurerJndi() throws Exception {
        // Balanser så CP-size = TaskThreads+1 + Antall Connections man ønsker
        System.setProperty("task.manager.runner.threads", "6");
        new EnvEntry("jdbc/defaultDS", DatasourceUtil.createDatasource(DatasourceRole.USER, 12));
    }

    void migrerDatabaser() {
        try (var dataSource = DatasourceUtil.createDatasource(DatasourceRole.ADMIN, 2)) {
            var flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:/db/migration/defaultDS")
                .baselineOnMigrate(true);
            if (ENV.isProd() || ENV.isDev()) {
                flyway.initSql(String.format("SET ROLE \"%s\"", DatasourceUtil.getRole(DatasourceRole.ADMIN)));
            }
            flyway.load().migrate();
        } catch (FlywayException e) {
            log.error("Feil under migrering av databasen.");
            throw e;
        }
    }

    private void start() throws Exception {
        var server = new Server(getServerPort());
        server.setConnectors(createConnectors(server).toArray(new Connector[]{}));
        var handlers = new HandlerList(new ResetLogContextHandler(), createContext());
        server.setHandler(handlers);
        server.start();
        server.join();
    }

    private List<Connector> createConnectors(Server server) {
        List<Connector> connectors = new ArrayList<>();
        var httpConnector = new ServerConnector(server, new HttpConnectionFactory(createHttpConfiguration()));
        httpConnector.setPort(getServerPort());
        connectors.add(httpConnector);
        return connectors;
    }

    private Integer getServerPort() {
        return this.serverPort;
    }

    /**
     * Legges først slik at alltid resetter context før prosesserer nye requests. Kjøres først så ikke risikerer andre har satt Request#setHandled(true).
     */
    static final class ResetLogContextHandler extends AbstractHandler {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            MDC.clear();
        }
    }
}
