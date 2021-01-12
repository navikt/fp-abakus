package no.nav.foreldrepenger.abakus.app.konfig;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import no.nav.foreldrepenger.abakus.lonnskomp.kafka.LonnskompConsumer;

/**
 * Triggers start of Kafka consum
 */
@WebListener
public class LønnskompensasjonConsumerStarter implements ServletContextListener {

    @Inject //NOSONAR
    private LonnskompConsumer lonnskompConsumer; //NOSONAR

    public LønnskompensasjonConsumerStarter() { //NOSONAR
        // For CDI
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // NOSONAR
        }
        lonnskompConsumer.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        lonnskompConsumer.stop();
    }
}
