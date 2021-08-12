package no.nav.foreldrepenger.abakus.jetty;

import static no.nav.vedtak.util.env.Cluster.LOCAL;
import static no.nav.vedtak.util.env.Cluster.NAIS_CLUSTER_NAME;

import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.security.auth.message.config.AuthConfigFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.geronimo.components.jaspi.AuthConfigFactoryImpl;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.jaas.JAASLoginService;
import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.jaspi.JaspiAuthenticatorFactory;
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
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.MetaData;
import org.eclipse.jetty.webapp.WebAppConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import no.nav.foreldrepenger.abakus.app.konfig.ApplicationConfig;
import no.nav.foreldrepenger.abakus.jetty.db.DatabaseScript;
import no.nav.foreldrepenger.abakus.jetty.db.DatasourceRole;
import no.nav.foreldrepenger.abakus.jetty.db.DatasourceUtil;
import no.nav.foreldrepenger.abakus.jetty.db.EnvironmentClass;
import no.nav.vedtak.isso.IssoApplication;
import no.nav.vedtak.util.env.Environment;

public class JettyServer {

    /** Legges først slik at alltid resetter context før prosesserer nye requests. Kjøres først så ikke risikerer andre har satt Request#setHandled(true). */
    static final class ResetLogContextHandler extends AbstractHandler {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            MDC.clear();
        }
    }

    /**
     * AbstractNetworkConnector#getHost()
     *
     * @see ServerConnector#openAcceptChannel()
     */
    protected static final String SERVER_HOST = "0.0.0.0";

    /**
     * nedstrippet sett med Jetty configurations for raskere startup.
     */
    protected static final Configuration[] CONFIGURATIONS = new Configuration[]{
        new WebAppConfiguration(),
        new WebInfConfiguration(),
        new WebXmlConfiguration(),
        new AnnotationConfiguration(),
        new EnvConfiguration(),
        new PlusConfiguration(),
    };

    private static final Environment ENV = Environment.current();

    private static final Logger log = LoggerFactory.getLogger(JettyServer.class);

    private AppKonfigurasjon appKonfigurasjon;

    public JettyServer() {
        this(new JettyWebKonfigurasjon());
    }

    public JettyServer(int serverPort) {
        this(new JettyWebKonfigurasjon(serverPort));
    }

    JettyServer(AppKonfigurasjon appKonfigurasjon) {
        this.appKonfigurasjon = appKonfigurasjon;
    }

    public static void main(String[] args) throws Exception {
        System.setProperty(NAIS_CLUSTER_NAME, ENV.clusterName());
        JettyServer jettyServer;

        if (args.length > 0) {
            int serverPort = Integer.parseUnsignedInt(args[0]);
            jettyServer = new JettyServer(serverPort);
        } else {
            jettyServer = new JettyServer();
        }
        jettyServer.bootStrap();
    }

    protected void bootStrap() throws Exception {
        migrerDatabaser();
        konfigurer();
        start(appKonfigurasjon);
    }

    protected void konfigurer() throws Exception {
        File jaspiConf = new File(System.getProperty("conf", "./conf") + "/jaspi-conf.xml"); // NOSONAR

        konfigurerSikkerhet(jaspiConf);
        konfigurerJndi();
    }

    protected void konfigurerSikkerhet(File jaspiConf) {
        Security.setProperty(AuthConfigFactory.DEFAULT_FACTORY_SECURITY_PROPERTY, AuthConfigFactoryImpl.class.getCanonicalName());

        if (!jaspiConf.exists()) {
            throw new IllegalStateException("Missing required file: " + jaspiConf.getAbsolutePath());
        }
        System.setProperty("org.apache.geronimo.jaspic.configurationFile", jaspiConf.getAbsolutePath());

    }

    protected void konfigurerJndi() throws Exception {
        new EnvEntry("jdbc/defaultDS",
                DatasourceUtil.createDatasource("defaultDS", DatasourceRole.USER, ENV.getCluster(), 7));
    }

    protected void migrerDatabaser() {
        String initSql = String.format("SET ROLE \"%s\"", DatasourceUtil.getDbRole("defaultDS", DatasourceRole.ADMIN));
        if (LOCAL.equals(ENV.getCluster())) {
            // TODO: Ønsker egentlig ikke dette, men har ikke satt opp skjema lokalt
            // til å ha en admin bruker som gjør migrering og en annen som gjør CRUD
            // operasjoner
            initSql = null;
        }
        DataSource migreringDs = DatasourceUtil.createDatasource("defaultDS", DatasourceRole.ADMIN, ENV.getCluster(),
                1);
        try {
            DatabaseScript.migrate(migreringDs, initSql);
            migreringDs.getConnection().close();
        } catch (SQLException e) {
            log.warn("Klarte ikke stenge connection etter migrering", e);
        }
    }

    private void start(AppKonfigurasjon appKonfigurasjon) throws Exception {
        Server server = new Server(appKonfigurasjon.getServerPort());
        server.setConnectors(createConnectors(appKonfigurasjon, server).toArray(new Connector[] {}));

        var handlers = new HandlerList(new ResetLogContextHandler(), createContext(appKonfigurasjon));
        server.setHandler(handlers);
        server.start();
        server.join();
    }

    protected EnvironmentClass getEnvironmentClass() {
        return EnvironmentUtil.getEnvironmentClass();
    }

    @SuppressWarnings("resource")
    protected List<Connector> createConnectors(AppKonfigurasjon appKonfigurasjon, Server server) {
        List<Connector> connectors = new ArrayList<>();
        ServerConnector httpConnector = new ServerConnector(server, new HttpConnectionFactory(createHttpConfiguration()));
        httpConnector.setPort(appKonfigurasjon.getServerPort());
        httpConnector.setHost(SERVER_HOST);
        connectors.add(httpConnector);

        return connectors;
    }

    @SuppressWarnings("resource")
    protected WebAppContext createContext(AppKonfigurasjon appKonfigurasjon) throws IOException {
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setParentLoaderPriority(true);

        // må hoppe litt bukk for å hente web.xml fra classpath i stedet for fra filsystem.
        String descriptor;
        try (var resource = Resource.newClassPathResource("/WEB-INF/web.xml")) {
            descriptor = resource.getURI().toURL().toExternalForm();
        }

        webAppContext.setDescriptor(descriptor);
        webAppContext.setBaseResource(createResourceCollection());
        webAppContext.setContextPath(appKonfigurasjon.getContextPath());
        webAppContext.setConfigurations(CONFIGURATIONS);

        webAppContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");

        /*
         * lar jetty scanne flere jars for web resources (eks. WebFilter/WebListener annotations),
         * men bare de som matchr pattern for raskere oppstart
         */
        webAppContext.setAttribute("org.eclipse.jetty.server.webapp.WebInfIncludeJarPattern", "^.*jersey-.*.jar$|^.*felles-sikkerhet-.*.jar$");
        webAppContext.setSecurityHandler(createSecurityHandler());

        updateMetaData(webAppContext.getMetaData());
        webAppContext.setThrowUnavailableOnStartupException(true);

        return webAppContext;
    }


    protected HttpConfiguration createHttpConfiguration() {
        // Create HTTP Config
        HttpConfiguration httpConfig = new HttpConfiguration();

        // Add support for X-Forwarded headers
        httpConfig.addCustomizer(new org.eclipse.jetty.server.ForwardedRequestCustomizer());

        return httpConfig;

    }

    private SecurityHandler createSecurityHandler() {
        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.setAuthenticatorFactory(new JaspiAuthenticatorFactory());

        JAASLoginService loginService = new JAASLoginService();
        loginService.setName("jetty-login");
        loginService.setLoginModuleName("jetty-login");
        loginService.setIdentityService(new DefaultIdentityService());
        securityHandler.setLoginService(loginService);

        return securityHandler;
    }

    private void updateMetaData(MetaData metaData) {
        // Find path to class-files while starting jetty from development environment.
        List<Class<?>> appClasses = getApplicationClasses();

        List<Resource> resources = appClasses.stream()
                .map(c -> Resource.newResource(c.getProtectionDomain().getCodeSource().getLocation()))
                .collect(Collectors.toList());

        metaData.setWebInfClassesResources(resources);
    }

    protected List<Class<?>> getApplicationClasses() {
        return Arrays.asList(ApplicationConfig.class, IssoApplication.class);
    }

    @SuppressWarnings("resource")
    private ResourceCollection createResourceCollection() throws IOException {
        return new ResourceCollection(
            Resource.newClassPathResource("META-INF/resources/webjars/"),
            Resource.newClassPathResource("/web"));
    }
}
