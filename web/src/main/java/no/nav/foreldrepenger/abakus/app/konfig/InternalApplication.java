package no.nav.foreldrepenger.abakus.app.konfig;

import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.swagger.jaxrs.config.BeanConfig;
import no.nav.foreldrepenger.abakus.app.selftest.NaisRestTjeneste;
import no.nav.foreldrepenger.abakus.app.selftest.SelftestRestTjeneste;

@ApplicationPath(InternalApplication.API_URL)
public class InternalApplication extends Application {

    public static final String API_URL = "internal";

    public InternalApplication() {
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0");
        if (utviklingServer()) {
            beanConfig.setSchemes(new String[]{"http"});
        } else {
            beanConfig.setSchemes(new String[]{"https"});
        }

        beanConfig.setBasePath("/fpabakus/" + API_URL);
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
        return Set.of(NaisRestTjeneste.class, SelftestRestTjeneste.class);
    }

}
