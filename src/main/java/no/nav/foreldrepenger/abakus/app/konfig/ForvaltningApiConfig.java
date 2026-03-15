package no.nav.foreldrepenger.abakus.app.konfig;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import jakarta.ws.rs.ApplicationPath;
import no.nav.foreldrepenger.abakus.app.vedlikehold.ForvaltningRestTjeneste;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;
import no.nav.vedtak.openapi.OpenApiUtils;
import no.nav.vedtak.server.rest.ForvaltningAuthorizationFilter;
import no.nav.vedtak.server.rest.FpRestJackson2Feature;
import no.nav.vedtak.server.rest.GeneralRestExceptionMapper;

@ApplicationPath(ForvaltningApiConfig.API_URI)
public class ForvaltningApiConfig extends ResourceConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ForvaltningApiConfig.class);
    public static final String API_URI = "/forvaltning/api";
    private static final Environment ENV = Environment.current();

    public ForvaltningApiConfig() {
        LOG.info("Initialiserer: {}", API_URI);
        GeneralRestExceptionMapper.setBrukerRettetApplikasjon(false);
        register(FpRestJackson2Feature.class);
        register(ForvaltningAuthorizationFilter.class);
        registerOpenApi();
        registerClasses(getApplicationClasses());
        setProperties(getApplicationProperties());
        LOG.info("Ferdig med initialisering av {}", API_URI);
    }

    private void registerOpenApi() {
        OpenApiUtils.setupOpenApi("Foreldrepenger Inntekt Arbeid Ytelse",
            ENV.getProperty("context.path", "/fpabakus"), getApplicationClasses(), this);
        register(OpenApiResource.class);
    }

    private Set<Class<?>> getApplicationClasses() {
        return Set.of(ProsessTaskRestTjeneste.class, ForvaltningRestTjeneste.class);
    }

    private Map<String, Object> getApplicationProperties() {
        Map<String, Object> properties = new HashMap<>();
        // Ref Jersey doc
        properties.put(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        properties.put(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, true);
        return properties;
    }

}
