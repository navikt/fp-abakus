package no.nav.foreldrepenger.abakus.app.konfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.hotspot.DefaultExports;
import no.nav.foreldrepenger.abakus.felles.kafka.KafkaIntegration;
import no.nav.vedtak.felles.integrasjon.sensu.SensuKlient;
import no.nav.vedtak.felles.prosesstask.impl.BatchTaskScheduler;
import no.nav.vedtak.felles.prosesstask.impl.TaskManager;

/**
 * Initialiserer applikasjontjenester som implementer AppServiceHandler
 */
@ApplicationScoped
public class ApplicationServiceStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceStarter.class);
    private Map<KafkaIntegration, AtomicBoolean> serviceMap = new HashMap<>();
    private SensuKlient sensuKlient;
    private TaskManager taskManager;
    private BatchTaskScheduler batchTaskScheduler;

    ApplicationServiceStarter() {
        // CDI
    }

    @Inject
    public ApplicationServiceStarter(@Any Instance<KafkaIntegration> serviceHandlers,
                                     SensuKlient sensuKlient,
                                     TaskManager taskManager,
                                     BatchTaskScheduler batchTaskScheduler) {
        this.sensuKlient = sensuKlient;
        this.taskManager = taskManager;
        this.batchTaskScheduler = batchTaskScheduler;
        serviceHandlers.forEach(handler -> serviceMap.put(handler, new AtomicBoolean()));
    }

    public void startServices() {
        DefaultExports.initialize();
        sensuKlient.start();
        taskManager.start();
        batchTaskScheduler.start();
        serviceMap.forEach((key, value) -> {
            if (value.compareAndSet(false, true)) {
                LOGGER.info("starter service: {}", key.getClass().getSimpleName());
                key.start();
            }
        });
    }

    public boolean isKafkaAlive() {
        return serviceMap.entrySet()
            .stream()
            .filter(it -> it.getKey() != null)
            .allMatch(it -> it.getKey().isAlive());
    }

    public void stopServices() {
        List<Thread> threadList = new ArrayList<>();
        serviceMap.forEach((key, value) -> {
            if (value.compareAndSet(true, false)) {
                LOGGER.info("stopper service: {}", key.getClass().getSimpleName());
                Thread t = new Thread(key::stop);
                t.start();
                threadList.add(t);
            }
        });
        while (!threadList.isEmpty()) {
            Thread t = threadList.get(0);
            try {
                t.join(31000);
                threadList.remove(t);
            } catch (InterruptedException e) {
                LOGGER.warn(e.getMessage());
                t.interrupt();
            }
        }

        batchTaskScheduler.stop();
        taskManager.stop();
        sensuKlient.stop();
    }

}
