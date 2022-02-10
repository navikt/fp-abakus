package no.nav.foreldrepenger.abakus.jetty;

import no.nav.vedtak.sikkerhet.ContextPathHolder;

public class JettyWebKonfigurasjon implements AppKonfigurasjon {
    private static final String CONTEXT_PATH = "/fpabakus";

    private Integer serverPort;

    public JettyWebKonfigurasjon() {
        ContextPathHolder.instance(CONTEXT_PATH);
    }

    public JettyWebKonfigurasjon(int serverPort) {
        this();
        this.serverPort = serverPort;
    }

    @Override
    public int getServerPort() {
        if (serverPort == null) {
            return DEFAULT_SERVER_PORT;
        }
        return serverPort;
    }

    @Override
    public String getContextPath() {
        return CONTEXT_PATH;
    }
}
