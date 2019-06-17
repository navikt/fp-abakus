package no.nav.foreldrepenger.abakus.kafka;

public interface KafkaIntegration {

    /**
     * Er integrasjonen i live.
     *
     * @return true / false
     */
    boolean isAlive();
}
