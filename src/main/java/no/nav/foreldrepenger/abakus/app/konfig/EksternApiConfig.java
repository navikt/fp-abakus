package no.nav.foreldrepenger.abakus.app.konfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.ApplicationPath;
import no.nav.foreldrepenger.abakus.app.rest.ekstern.EksternDelingAvYtelserRestTjeneste;
import no.nav.vedtak.server.rest.FpRestJackson2Feature;

@ApplicationPath(EksternApiConfig.API_URI)
public class EksternApiConfig extends ResourceConfig {

    private static final Logger LOG = LoggerFactory.getLogger(EksternApiConfig.class);
    public static final String API_URI = "/ekstern/api";

    public EksternApiConfig() {
        LOG.info("Initialiserer: {}", API_URI);
        setApplicationName(EksternApiConfig.class.getSimpleName());
        register(FpRestJackson2Feature.class);
        registerClasses(getEksternalApplicationClasses());
        setProperties(getApplicationProperties());
        LOG.info("Ferdig med initialisering av {}", API_URI);
    }

    private Set<Class<?>> getEksternalApplicationClasses() {
        // eksponert grensesnitt
        return Set.of(EksternDelingAvYtelserRestTjeneste.class);
    }

    private Map<String, Object> getApplicationProperties() {
        Map<String, Object> properties = new HashMap<>();
        // Ref Jersey doc
        properties.put(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        properties.put(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, true);
        return properties;
    }

}
