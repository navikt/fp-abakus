package no.nav.foreldrepenger.abakus.app.tjenester;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Starter interne applikasjontjenester
 */
@WebListener
public class ServiceStarterListener implements ServletContextListener {

    @Inject
    private ApplicationServiceStarter applicationServiceStarter;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        applicationServiceStarter.startServices();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        applicationServiceStarter.stopServices();
    }
}
