package no.nav.foreldrepenger.abakus.vedtak.kafka;

import java.util.Objects;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.kafka.common.serialization.Serdes;

import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
class VedtakStreamKafkaProperties {

    private final String bootstrapServers;
    private final String schemaRegistryUrl;
    private final String clientId;
    private final String username;
    private final String password;
    private final String topic;

    @Inject
    VedtakStreamKafkaProperties(@KonfigVerdi("kafka.topic.vedtakfattet") String topic,
                                @KonfigVerdi("kafka.boostrap.servers") String bootstrapServers,
                                @KonfigVerdi("kafka.schema.registry.url") String schemaRegistryUrl,
                                @KonfigVerdi("application.name") String clientId,
                                @KonfigVerdi("systembruker.username") String username,
                                @KonfigVerdi("systembruker.password") String password) {
        Objects.requireNonNull(topic, "topic");
        this.topic = topic;
        this.bootstrapServers = bootstrapServers;
        this.schemaRegistryUrl = schemaRegistryUrl;
        this.clientId = clientId;
        this.username = username;
        this.password = password;
    }

    String getBootstrapServers() {
        return bootstrapServers;
    }

    String getSchemaRegistryUrl() {
        return schemaRegistryUrl;
    }

    String getClientId() {
        return clientId;
    }

    String getUsername() {
        return username;
    }

    String getPassword() {
        return password;
    }

    String getTopic() {
        return topic;
    }

    Class<?> getKeyClass() {
        return Serdes.String().getClass();
    }

    Class<?> getValueClass() {
        return Serdes.String().getClass();
    }

    boolean harSattBrukernavn() {
        return username != null && !username.isEmpty();
    }
}
