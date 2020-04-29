package no.nav.foreldrepenger.abakus.felles;

import java.time.Duration;
import java.util.Objects;

import no.nav.foreldrepenger.abakus.felles.metrikker.MetrikkerTjeneste;

public class FellesRestTjeneste {

    private MetrikkerTjeneste metrikkTjeneste;

    public FellesRestTjeneste() {} // RESTEASY ctor

    public FellesRestTjeneste(MetrikkerTjeneste metrikkTjeneste) {
        this.metrikkTjeneste = Objects.requireNonNull(metrikkTjeneste, "MetrikkerTjeneste forventes.");
    }

    protected void logMetrikk(String ressurs, Duration executionTime) {
        metrikkTjeneste.logRestKall(ressurs, executionTime.toNanos());
    }

    protected MetrikkerTjeneste getMetrikkTjeneste() {
        return metrikkTjeneste;
    }
}
