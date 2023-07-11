package no.nav.foreldrepenger.abakus.app.konfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import org.glassfish.jersey.server.ServerProperties;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import no.nav.foreldrepenger.abakus.app.diagnostikk.DiagnostikkRestTjeneste;
import no.nav.foreldrepenger.abakus.app.diagnostikk.rapportering.RapporteringRestTjeneste;
import no.nav.foreldrepenger.abakus.app.exceptions.ConstraintViolationMapper;
import no.nav.foreldrepenger.abakus.app.exceptions.GeneralRestExceptionMapper;
import no.nav.foreldrepenger.abakus.app.exceptions.JsonMappingExceptionMapper;
import no.nav.foreldrepenger.abakus.app.exceptions.JsonParseExceptionMapper;
import no.nav.foreldrepenger.abakus.app.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.abakus.app.vedlikehold.ForvaltningRestTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.ArbeidsforholdRestTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.GrunnlagRestTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.InntektsmeldingerRestTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.OppgittOpptjeningRestTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.OppgittOpptjeningV2RestTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.tjeneste.RegisterdataRestTjeneste;
import no.nav.foreldrepenger.abakus.vedtak.tjeneste.YtelseRestTjeneste;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;

@ApplicationPath(ApiConfig.API_URI)
public class ApiConfig extends Application {

    public static final String API_URI = "/api";
    private static final Environment ENV = Environment.current();
    private static final String ID_PREFIX = "openapi.context.id.servlet.";


    public ApiConfig() {
        OpenAPI oas = new OpenAPI();
        Info info = new Info().title("Vedtaksløsningen - Abakus").version("1.0").description("REST grensesnitt for Vedtaksløsningen.");

        oas.info(info).addServersItem(new Server().url(ENV.getProperty("context.path", "/fpabakus")));
        var oasConfig = new SwaggerConfiguration().id(ID_PREFIX + ApiConfig.class.getName())
            .openAPI(oas)
            .prettyPrint(true)
            .resourceClasses(getClasses().stream().map(Class::getName).collect(Collectors.toSet()))
            .ignoredRoutes(Set.of("/api/ytelse/v1/hent-vedtatte/for-ident/k9", "/api/ytelse/v1/hent-vedtatte-og-historiske/for-ident/k9",
                "/api/ytelse/v1/hent-vedtatte/for-ident"));

        try {
            new JaxrsOpenApiContextBuilder<>().ctxId(ID_PREFIX + ApiConfig.class.getName())
                .application(this)
                .openApiConfiguration(oasConfig)
                .buildContext(true)
                .read();
        } catch (OpenApiConfigurationException e) {
            throw new TekniskException("OPEN-API", e.getMessage(), e);
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        // eksponert grensesnitt

        return Set.of(ProsessTaskRestTjeneste.class, RegisterdataRestTjeneste.class, InntektsmeldingerRestTjeneste.class,
            OppgittOpptjeningRestTjeneste.class, OppgittOpptjeningV2RestTjeneste.class, GrunnlagRestTjeneste.class, ArbeidsforholdRestTjeneste.class,
            YtelseRestTjeneste.class,

            ForvaltningRestTjeneste.class, DiagnostikkRestTjeneste.class, RapporteringRestTjeneste.class,

            OpenApiResource.class, JacksonJsonConfig.class, ConstraintViolationMapper.class, JsonMappingExceptionMapper.class,
            JsonParseExceptionMapper.class, GeneralRestExceptionMapper.class);
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        // Ref Jersey doc
        properties.put(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        properties.put(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, true);
        return properties;
    }

}
