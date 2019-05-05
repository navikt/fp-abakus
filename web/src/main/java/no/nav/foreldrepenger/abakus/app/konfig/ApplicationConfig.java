package no.nav.foreldrepenger.abakus.app.konfig;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.swagger.jaxrs.config.BeanConfig;
import no.nav.foreldrepenger.abakus.app.exceptions.ConstraintViolationMapper;
import no.nav.foreldrepenger.abakus.app.exceptions.GeneralRestExceptionMapper;
import no.nav.foreldrepenger.abakus.app.exceptions.JsonMappingExceptionMapper;
import no.nav.foreldrepenger.abakus.app.exceptions.JsonParseExceptionMapper;
import no.nav.foreldrepenger.abakus.app.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.abakus.iay.tjeneste.ArbeidsforholdRestTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.GrunnlagRestTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.tjeneste.RegistedataRestTjeneste;
import no.nav.foreldrepenger.abakus.vedtak.tjeneste.YtelseRestTjeneste;

@ApplicationPath(ApplicationConfig.API_URI)
public class ApplicationConfig extends Application {

    public static final String API_URI = "/api";

    public ApplicationConfig() {
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0");
        if (utviklingServer()) {
            beanConfig.setSchemes(new String[]{"http"});
        } else {
            beanConfig.setSchemes(new String[]{"https"});
        }

        beanConfig.setBasePath("/fpabakus" + API_URI);
        beanConfig.setResourcePackage("no.nav");
        beanConfig.setTitle("Vedtaksløsningen - Abakus");
        beanConfig.setDescription("REST grensesnitt for Vedtaksløsningen.");
        beanConfig.setScan(true);
    }

    /**
     * Finner ut av om vi kjører utviklingsserver. Settes i JettyDevServer#konfigurerMiljø()
     *
     * @return true dersom utviklingsserver.
     */
    private boolean utviklingServer() {
        return Boolean.getBoolean("develop-local");
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        classes.add(RegistedataRestTjeneste.class);
        classes.add(GrunnlagRestTjeneste.class);
        classes.add(ArbeidsforholdRestTjeneste.class);
        classes.add(YtelseRestTjeneste.class);

        classes.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        classes.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);

        classes.add(ConstraintViolationMapper.class);
        classes.add(JsonMappingExceptionMapper.class);
        classes.add(JsonParseExceptionMapper.class);
        classes.add(GeneralRestExceptionMapper.class);
        classes.add(JacksonJsonConfig.class);

        return Collections.unmodifiableSet(classes);
    }
}
