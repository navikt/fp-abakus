package no.nav.foreldrepenger.abakus.vedtak.kafka;

public interface KafkaIntegration {

    /**
     * Er integrasjonen i live.
     *
     * @return true / false
     */
    boolean isAlive();
}
