package no.nav.foreldrepenger.abakus.app.konfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.glassfish.jersey.server.ServerProperties;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import no.nav.foreldrepenger.abakus.app.exceptions.ConstraintViolationMapper;
import no.nav.foreldrepenger.abakus.app.exceptions.GeneralRestExceptionMapper;
import no.nav.foreldrepenger.abakus.app.exceptions.JsonMappingExceptionMapper;
import no.nav.foreldrepenger.abakus.app.exceptions.JsonParseExceptionMapper;
import no.nav.foreldrepenger.abakus.app.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.abakus.app.rest.ekstern.EksternDelingAvYtelserRestTjeneste;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;

@ApplicationPath(EksternApiConfig.API_URI)
public class EksternApiConfig extends Application {

    public static final String API_URI = "/ekstern/api";
    private static final Environment ENV = Environment.current();
    private static final String ID_PREFIX = "openapi.context.id.servlet.";

    public EksternApiConfig() {
        var oas = new OpenAPI();
        var info = new Info().title("Vedtaksløsningen - Abakus - Ekstern")
            .version("1.0")
            .description("Ekstern REST grensesnitt for Abakus. Alle kall må authentiseres med en gyldig Azure OBO eller CC token.");

        oas.info(info).addServersItem(new Server().url(ENV.getProperty("context.path", "/fpabakus")));
        var oasConfig = new SwaggerConfiguration().id(ID_PREFIX + EksternApiConfig.class.getName())
            .openAPI(oas)
            .prettyPrint(true)
            .resourceClasses(getClasses().stream().map(Class::getName).collect(Collectors.toSet()));

        try {
            new JaxrsOpenApiContextBuilder<>().ctxId(ID_PREFIX + EksternApiConfig.class.getName())
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

        return Set.of(EksternDelingAvYtelserRestTjeneste.class,
            // Applikasjonsoppsett
            AuthenticationFilter.class,
            JacksonJsonConfig.class,
            // Swagger
            OpenApiResource.class,
            // ExceptionMappers pga de som finnes i Jackson+Jersey-media
            ConstraintViolationMapper.class, JsonMappingExceptionMapper.class, JsonParseExceptionMapper.class,
            // Generell exceptionmapper m/logging for øvrige tilfelle
            GeneralRestExceptionMapper.class);
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
