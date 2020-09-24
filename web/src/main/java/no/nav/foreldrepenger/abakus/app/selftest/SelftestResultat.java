package no.nav.foreldrepenger.abakus.app.selftest;

public class SelftestResultat {

    private boolean critical;
    private boolean ready;
    private String description;
    private String endpoint;

    public SelftestResultat(boolean critical, boolean ready, String description, String endpoint) {
        this.critical = critical;
        this.ready = ready;
        this.description = description;
        this.endpoint = endpoint;
    }

    public boolean isCritical() {
        return critical;
    }

    public boolean isReady() {
        return ready;
    }

    public String getDescription() {
        return description;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
