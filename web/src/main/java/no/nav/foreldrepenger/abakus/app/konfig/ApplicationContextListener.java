package no.nav.foreldrepenger.abakus.app.konfig;

import javax.enterprise.inject.spi.CDI;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starter interne applikasjontjenester
 */
@WebListener
public class ApplicationContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // start denne async, logg til slutt når ferdig
        Thread thread = new Thread(this::startServices, getClass().getSimpleName() + "-thread");
        thread.setDaemon(true);
        thread.start();
    }

    private void startServices() {
        // Henter dependent instance og destroyer etterpå.
        ApplicationServiceStarter serviceStarter = null;
        try {
            Thread.sleep(2000); // La verden gå litt videre får vi dumper ut
            serviceStarter = CDI.current().select(ApplicationServiceStarter.class).get();
            serviceStarter.startServices();
        } catch (InterruptedException e) {
            logger.warn("Feil under oppstart av services", e);
            throw new RuntimeException(e);
        } finally {
            if (serviceStarter != null) {
                CDI.current().destroy(serviceStarter);
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        final var serviceStarter = CDI.current().select(ApplicationServiceStarter.class).get();
        serviceStarter.stopServices();
    }


}
