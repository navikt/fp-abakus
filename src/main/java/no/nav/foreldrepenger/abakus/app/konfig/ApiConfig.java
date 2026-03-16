package no.nav.foreldrepenger.abakus.app.konfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import no.nav.foreldrepenger.abakus.kobling.KoblingRestTjeneste;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.ApplicationPath;
import no.nav.foreldrepenger.abakus.iay.tjeneste.ArbeidsforholdRestTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.GrunnlagRestTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.InntektsmeldingerRestTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.OppgittOpptjeningRestTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.tjeneste.RegisterdataRestTjeneste;
import no.nav.foreldrepenger.abakus.vedtak.tjeneste.YtelseRestTjeneste;
import no.nav.vedtak.server.rest.FpRestJackson2Feature;

@ApplicationPath(ApiConfig.API_URI)
public class ApiConfig extends ResourceConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ApiConfig.class);
    public static final String API_URI = "/api";

    public ApiConfig() {
        LOG.info("Initialiserer: {}", API_URI);
        setApplicationName(ApiConfig.class.getSimpleName());
        register(FpRestJackson2Feature.class);
        registerClasses(getApplicationClasses());
        setProperties(getApplicationProperties());
        LOG.info("Ferdig med initialisering av {}", API_URI);
    }

    private Set<Class<?>> getApplicationClasses() {
        // eksponert grensesnitt
        return Set.of(RegisterdataRestTjeneste.class,
            InntektsmeldingerRestTjeneste.class,
            OppgittOpptjeningRestTjeneste.class,
            GrunnlagRestTjeneste.class,
            ArbeidsforholdRestTjeneste.class,
            YtelseRestTjeneste.class,
            KoblingRestTjeneste.class);
    }

    private Map<String, Object> getApplicationProperties() {
        Map<String, Object> properties = new HashMap<>();
        // Ref Jersey doc
        properties.put(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        properties.put(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, true);
        return properties;
    }

}
