package no.nav.foreldrepenger.abakus.felles.kafka;

import no.nav.vedtak.apptjeneste.AppServiceHandler;

public interface KafkaIntegration extends AppServiceHandler {

    /**
     * Er integrasjonen i live.
     *
     * @return true / false
     */
    boolean isAlive();
}
