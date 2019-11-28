package no.nav.foreldrepenger.abakus.app.konfig;

import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import no.nav.foreldrepenger.abakus.app.metrics.PrometheusRestService;
import no.nav.foreldrepenger.abakus.app.selftest.NaisRestTjeneste;
import no.nav.foreldrepenger.abakus.app.selftest.SelftestRestTjeneste;

@ApplicationPath(InternalApplication.API_URL)
public class InternalApplication extends Application {

    public static final String API_URL = "internal";

    public InternalApplication() {

    }

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(PrometheusRestService.class,
            NaisRestTjeneste.class,
            SelftestRestTjeneste.class);
    }

}
