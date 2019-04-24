package no.nav.foreldrepenger.abakus.vedtak.kafka;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.familie.topic.Topic;
import no.nav.familie.topic.TopicManifest;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
class VedtakStreamKafkaProperties {

    private final String bootstrapServers;
    private final String schemaRegistryUrl;
    private final String clientId;
    private final String username;
    private final String password;
    private final Topic topic;

    @Inject
    VedtakStreamKafkaProperties(@KonfigVerdi("kafka.boostrap.servers") String bootstrapServers,
                                @KonfigVerdi("kafka.schema.registry.url") String schemaRegistryUrl,
                                @KonfigVerdi("kafka.consume.vedtak.clientId") String clientId,
                                @KonfigVerdi("systembruker.username") String username,
                                @KonfigVerdi("systembruker.password") String password) {

        this.topic = TopicManifest.FATTET_VEDTAK;
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
        return topic.getTopic();
    }

    Class<?> getKeyClass() {
        return topic.getSerdeKey().getClass();
    }

    Class<?> getValueClass() {
        return topic.getSerdeValue().getClass();
    }

    boolean harSattBrukernavn() {
        return username != null && !username.isEmpty();
    }
}
