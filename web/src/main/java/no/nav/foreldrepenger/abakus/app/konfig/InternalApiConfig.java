package no.nav.foreldrepenger.abakus.app.konfig;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.Set;
import no.nav.foreldrepenger.abakus.app.healthcheck.HealthCheckRestService;
import no.nav.foreldrepenger.abakus.app.metrics.PrometheusRestService;

@ApplicationPath(InternalApiConfig.API_URI)
public class InternalApiConfig extends Application {

    public static final String API_URI = "/internal";

    public InternalApiConfig() {
        // CDI
    }

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(PrometheusRestService.class, HealthCheckRestService.class);
    }
}
