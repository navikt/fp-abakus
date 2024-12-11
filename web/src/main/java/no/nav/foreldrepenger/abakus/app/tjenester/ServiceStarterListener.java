package no.nav.foreldrepenger.abakus.app.tjenester;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Starter interne applikasjontjenester
 */
@WebListener
public class ServiceStarterListener implements ServletContextListener {

    @Inject
    private ApplicationServiceStarter applicationServiceStarter;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        CDI.current().select(ApplicationServiceStarter.class).get().startServices();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        CDI.current().select(ApplicationServiceStarter.class).get().stopServices();
    }
}
