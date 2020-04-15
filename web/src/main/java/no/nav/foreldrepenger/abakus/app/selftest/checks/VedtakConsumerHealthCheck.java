package no.nav.foreldrepenger.abakus.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.streams.KafkaStreams;

import no.nav.foreldrepenger.abakus.vedtak.kafka.VedtakConsumer;

@ApplicationScoped
public class VedtakConsumerHealthCheck extends ExtHealthCheck {

    private VedtakConsumer consumer;

    VedtakConsumerHealthCheck() {
    }

    @Inject
    public VedtakConsumerHealthCheck(VedtakConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    protected String getDescription() {
        return "Consumer av vedtak fra fpsak.";
    }

    @Override
    protected String getEndpoint() {
        return consumer.getTopic();
    }

    @Override
    protected InternalResult performCheck() {
        InternalResult intTestRes = new InternalResult();

        KafkaStreams.State tilstand = consumer.getTilstand();
        intTestRes.setMessage("Consumer is in state [" + tilstand.name() + "].");
        if (tilstand.isRunningOrRebalancing() || KafkaStreams.State.CREATED.equals(tilstand)) {
            intTestRes.setOk(true);
        } else {
            intTestRes.setOk(false);
        }
        intTestRes.noteResponseTime();

        return intTestRes;
    }
}
