package no.nav.foreldrepenger.abakus.felles;

import no.nav.foreldrepenger.abakus.felles.metrikker.MetrikkerTjeneste;

public class FellesRestTjeneste {

    private MetrikkerTjeneste metrikkTjeneste;

    public FellesRestTjeneste() {} // RESTEASY ctor

    public FellesRestTjeneste(MetrikkerTjeneste metrikkTjeneste) {
        this.metrikkTjeneste = metrikkTjeneste;
    }

    protected void logMetrikk(String ressurs) {
        metrikkTjeneste.logRestKall(ressurs);
    }
}
