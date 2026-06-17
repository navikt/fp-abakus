package no.nav.foreldrepenger.abakus.jetty;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.server.localdev.LocalDevProperties;

public class JettyDevServer extends JettyServer {

    private static final Environment ENV = Environment.current();

    private JettyDevServer(int serverPort) {
        super(serverPort);
    }

    static void main(String[] args) throws Exception {
        LocalDevProperties.setPropertiesForLocalDev();
        jettyServer(args).bootStrap();
    }

    static JettyDevServer jettyServer(String[] args) {
        if (args.length > 0) {
            return new JettyDevServer(Integer.parseUnsignedInt(args[0]));
        }
        return new JettyDevServer(ENV.getProperty("server.port", Integer.class, 8015));
    }

}
