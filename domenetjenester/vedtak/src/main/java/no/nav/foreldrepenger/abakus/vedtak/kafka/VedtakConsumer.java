package no.nav.foreldrepenger.abakus.vedtak.kafka;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.felles.kafka.KafkaIntegration;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.kafka.KafkaProperties;

@ApplicationScoped
public class VedtakConsumer implements KafkaIntegration {

    private static final Logger log = LoggerFactory.getLogger(VedtakConsumer.class);

    private static final String APPLICATION_ID = "fpabakus"; // Hold konstant pga offset commit

    private KafkaStreams stream;
    private String topic;

    VedtakConsumer() {
    }

    @Inject
    public VedtakConsumer(@KonfigVerdi(value = "kafka.fattevedtak.topic", defaultVerdi = "teamforeldrepenger.familie-vedtakfattet-v1") String topicName,
                          VedtaksHendelseHåndterer vedtaksHendelseHåndterer) {
        this.topic = topicName;

        final Consumed<String, String> consumed = Consumed.with(Topology.AutoOffsetReset.EARLIEST);

        final StreamsBuilder builder = new StreamsBuilder();
        builder.stream(topic, consumed).foreach(vedtaksHendelseHåndterer::handleMessage);

        this.stream = new KafkaStreams(builder.build(), KafkaProperties.forStreamsStringValue(APPLICATION_ID));
    }

    private void addShutdownHooks() {
        stream.setStateListener((newState, oldState) -> {
            log.info("{} :: From state={} to state={}", topic, oldState, newState);

            if (newState == KafkaStreams.State.ERROR) {
                // if the stream has died there is no reason to keep spinning
                log.warn("{} :: No reason to keep living, closing stream", topic);
                stop();
            }
        });
        stream.setUncaughtExceptionHandler((t, e) -> {
            log.error(topic + " :: Caught exception in stream, exiting", e);
            stop();
        });
    }


    @Override
    public boolean isAlive() {
        return stream != null && stream.state().isRunningOrRebalancing();
    }

    public String getTopic() {
        return topic;
    }

    @Override
    public void start() {
        addShutdownHooks();
        stream.start();
        log.info("Starter konsumering av topic={}, tilstand={}", topic, stream.state());
    }

    @Override
    public void stop() {
        log.info("Starter shutdown av topic={}, tilstand={} med 10 sekunder timeout", topic, stream.state());
        stream.close(Duration.of(30, ChronoUnit.SECONDS));
        log.info("Shutdown av topic={}, tilstand={} med 10 sekunder timeout", topic, stream.state());
    }
}
