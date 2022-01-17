package no.nav.foreldrepenger.abakus.jetty;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.jetty.db.DatasourceRole;
import no.nav.foreldrepenger.abakus.jetty.db.DatasourceUtil;
import no.nav.foreldrepenger.abakus.jetty.db.EnvironmentClass;
import no.nav.foreldrepenger.konfig.Cluster;

public class JettyDevServer extends JettyServer {
    private static final Logger log = LoggerFactory.getLogger(JettyDevServer.class);

    /**
     * @see https://docs.oracle.com/en/java/javase/11/security/java-secure-socket-extension-jsse-reference-guide.html
     */
    private static final String TRUSTSTORE_PASSW_PROP = "javax.net.ssl.trustStorePassword";
    private static final String TRUSTSTORE_PATH_PROP = "javax.net.ssl.trustStore";

    public JettyDevServer() {
        super(new JettyDevKonfigurasjon());
    }

    public static void main(String[] args) throws Exception {
        JettyDevServer devServer = new JettyDevServer();
        devServer.bootStrap();
    }

    private static String initCryptoStoreConfig(String storeName, String storeProperty, String storePasswordProperty, String defaultPassword) {
        String defaultLocation = getProperty("user.home", ".") + "/.modig/" + storeName + ".jks";

        String storePath = getProperty(storeProperty, defaultLocation);
        File storeFile = new File(storePath);
        if (!storeFile.exists()) {
            throw new IllegalStateException("Finner ikke " + storeName + " i " + storePath
                + "\n\tKonfigurer enten som System property \'" + storeProperty + "\' eller environment variabel \'"
                + storeProperty.toUpperCase().replace('.', '_') + "\'");
        }
        String password = getProperty(storePasswordProperty, defaultPassword);
        if (password == null) {
            throw new IllegalStateException("Passord for å aksessere store " + storeName + " i " + storePath + " er null");
        }

        System.setProperty(storeProperty, storeFile.getAbsolutePath());
        System.setProperty(storePasswordProperty, password);
        return storePath;
    }

    private static String getProperty(String key, String defaultValue) {
        String val = System.getProperty(key, defaultValue);
        if (val == null) {
            val = System.getenv(key.toUpperCase().replace('.', '_'));
            val = val == null ? defaultValue : val;
        }
        return val;
    }

    @Override
    protected void migrerDatabaser() {
        try {
            super.migrerDatabaser();
        } catch (IllegalStateException e) {
            log.info("Migreringer feilet, cleaner og prøver på nytt for lokal db.");
            DataSource migreringDs = DatasourceUtil.createDatasource("defaultDS", DatasourceRole.ADMIN, Cluster.LOCAL, 1);
            try {
                DevDatabaseScript.clean(migreringDs);
            } finally {
                try {
                    migreringDs.getConnection().close();
                } catch (SQLException sqlException) {
                    log.warn("Klarte ikke stenge connection etter migrering", sqlException);
                }
            }
            super.migrerDatabaser();
        }
    }

    @Override
    protected EnvironmentClass getEnvironmentClass() {
        return EnvironmentClass.LOCALHOST;
    }

    @Override
    protected void bootStrap() throws Exception {
        System.setProperty("develop-local", "true");
        PropertiesUtils.initProperties();

        JettyDevDbKonfigurasjon konfig = new JettyDevDbKonfigurasjon();
        System.setProperty("defaultDS.url", konfig.getUrl());
        System.setProperty("defaultDS.username", konfig.getUser()); // benyttes kun hvis vault.enable=false
        System.setProperty("defaultDS.password", konfig.getPassword()); // benyttes kun hvis vault.enable=false

        super.bootStrap();
    }

    @Override
    protected void konfigurerSikkerhet() {
        // overstyrer angitt dir for lokal testing
        super.konfigurerSikkerhet();

        // truststore avgjør hva vi stoler på av sertifikater når vi gjør utadgående TLS kall
        initCryptoStoreConfig("truststore", TRUSTSTORE_PATH_PROP, TRUSTSTORE_PASSW_PROP, "changeit");
    }

    @SuppressWarnings("resource")
    @Override
    protected List<Connector> createConnectors(AppKonfigurasjon appKonfigurasjon, Server server) {
        List<Connector> connectors = super.createConnectors(appKonfigurasjon, server);
        HttpConfiguration https = createHttpConfiguration();
        https.addCustomizer(new SecureRequestCustomizer());

        return connectors;
    }

    @Override
    protected WebAppContext createContext(AppKonfigurasjon appKonfigurasjon) throws IOException {
        WebAppContext webAppContext = super.createContext(appKonfigurasjon);
        // https://www.eclipse.org/jetty/documentation/9.4.x/troubleshooting-locked-files-on-windows.html
        webAppContext.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        return webAppContext;
    }

}
