package no.nav.foreldrepenger.abakus.app.konfig;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.GenericOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import no.nav.foreldrepenger.abakus.app.exceptions.ConstraintViolationMapper;
import no.nav.foreldrepenger.abakus.app.exceptions.GeneralRestExceptionMapper;
import no.nav.foreldrepenger.abakus.app.exceptions.JsonMappingExceptionMapper;
import no.nav.foreldrepenger.abakus.app.exceptions.JsonParseExceptionMapper;
import no.nav.foreldrepenger.abakus.app.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.abakus.iay.tjeneste.ArbeidsforholdRestTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.GrunnlagRestTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.InntektsmeldingerRestTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.OppgittOpptjeningRestTjeneste;
import no.nav.foreldrepenger.abakus.kodeverk.tjeneste.KodeverkRestTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.tjeneste.RegisterdataRestTjeneste;
import no.nav.foreldrepenger.abakus.vedtak.tjeneste.YtelseRestTjeneste;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;

@ApplicationPath(ApplicationConfig.API_URI)
public class ApplicationConfig extends Application {

    public static final String API_URI = "/api";

    public ApplicationConfig() {
        OpenAPI oas = new OpenAPI();
        Info info = new Info()
            .title("Vedtaksløsningen - Abakus")
            .version("1.0")
            .description("REST grensesnitt for Vedtaksløsningen.");

        oas.info(info)
            .addServersItem(new Server()
                .url("/fpabakus"));
        SwaggerConfiguration oasConfig = new SwaggerConfiguration()
            .openAPI(oas)
            .prettyPrint(true)
            .scannerClass("io.swagger.v3.jaxrs2.integration.JaxrsAnnotationScanner")
            .resourcePackages(Stream.of("no.nav.vedtak", "no.nav.foreldrepenger")
                .collect(Collectors.toSet()));

        try {
            new GenericOpenApiContextBuilder()
                .openApiConfiguration(oasConfig)
                .buildContext(true)
                .read();
        } catch (OpenApiConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        classes.add(ProsessTaskRestTjeneste.class);
        classes.add(RegisterdataRestTjeneste.class);
        classes.add(InntektsmeldingerRestTjeneste.class);
        classes.add(OppgittOpptjeningRestTjeneste.class);
        classes.add(KodeverkRestTjeneste.class);
        classes.add(GrunnlagRestTjeneste.class);
        classes.add(ArbeidsforholdRestTjeneste.class);
        classes.add(YtelseRestTjeneste.class);
        classes.add(OpenApiResource.class);

        classes.add(ConstraintViolationMapper.class);
        classes.add(JsonMappingExceptionMapper.class);
        classes.add(JsonParseExceptionMapper.class);
        classes.add(GeneralRestExceptionMapper.class);
        classes.add(JacksonJsonConfig.class);

        return Collections.unmodifiableSet(classes);
    }
}
