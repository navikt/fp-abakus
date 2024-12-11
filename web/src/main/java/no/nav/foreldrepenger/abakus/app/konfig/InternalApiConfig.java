package no.nav.foreldrepenger.abakus.app.konfig;

import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.ApplicationPath;
import no.nav.foreldrepenger.abakus.app.healthcheck.HealthCheckRestService;
import no.nav.foreldrepenger.abakus.app.metrics.PrometheusRestService;

@ApplicationPath(InternalApiConfig.API_URI)
public class InternalApiConfig extends ResourceConfig {

    private static final Logger LOG = LoggerFactory.getLogger(InternalApiConfig.class);
    public static final String API_URI = "/internal";

    public InternalApiConfig() {
        LOG.info("Initialiserer: {}", API_URI);
        setApplicationName(InternalApiConfig.class.getSimpleName());
        register(HealthCheckRestService.class);
        register(PrometheusRestService.class);
        LOG.info("Ferdig med initialisering av {}", API_URI);
    }
}
