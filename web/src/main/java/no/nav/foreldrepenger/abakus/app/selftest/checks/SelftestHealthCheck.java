package no.nav.foreldrepenger.abakus.app.selftest.checks;

public interface SelftestHealthCheck {

    boolean isCritical();

    boolean isReady();

    String getDescription();

    String getEndpoint();

}
