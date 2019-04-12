package no.nav.foreldrepenger.abakus.vedtak.kafka;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.LogAndFailExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.vedtak.LagreVedtakTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class VedtakConsumer {

    private static final Logger log = LoggerFactory.getLogger(VedtakConsumer.class);
    private KafkaStreams stream;
    private String topic;
    private ProsessTaskRepository taskRepository;

    VedtakConsumer() {
    }

    @Inject
    public VedtakConsumer(ProsessTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        //this.topic = topic;

        Properties props = new Properties();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.POLL_MS_CONFIG, "100");
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndFailExceptionHandler.class);

        final StreamsBuilder builder = new StreamsBuilder();

        Consumed<String, String> stringStringConsumed = Consumed.with(Topology.AutoOffsetReset.EARLIEST);
        builder.stream(this.topic, stringStringConsumed)
            .foreach(this::handleMessage);

        final Topology topology = builder.build();
        stream = new KafkaStreams(topology, props);
    }

    private void handleMessage(String key, String payload) {
        ProsessTaskData data = new ProsessTaskData(LagreVedtakTask.TASKTYPE);
        data.setProperty(LagreVedtakTask.KEY, key);
        data.setPayload(payload);

        taskRepository.lagre(data);
    }

    public void start() {
        stream.start();
        log.info("Starter konsumering av {}, tilstand={}", topic, stream.state());
    }

    public void stop() {
        log.info("Starter shutdown av {}, tilstand={} med 10 sekunder timeout", topic, stream.state());
        stream.close(10, TimeUnit.SECONDS);
        log.info("Shutdown av {}, tilstand={} med 10 sekunder timeout", topic, stream.state());
    }
}
