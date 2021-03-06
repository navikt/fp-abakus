package no.nav.foreldrepenger.abakus.jetty;

public class JettyDevKonfigurasjon extends JettyWebKonfigurasjon {
    private static final int SSL_SERVER_PORT = 8043;
    private static final int DEV_SERVER_PORT = 8015;

    public JettyDevKonfigurasjon() {
        super(DEV_SERVER_PORT);
    }

    @Override
    public int getSslPort() {
        return SSL_SERVER_PORT;
    }

}
