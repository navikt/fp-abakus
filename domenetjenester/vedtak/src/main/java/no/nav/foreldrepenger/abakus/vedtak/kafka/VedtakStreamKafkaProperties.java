package no.nav.foreldrepenger.abakus.vedtak.kafka;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.abakus.topic.Topic;
import no.nav.abakus.topic.TopicManifest;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
class VedtakStreamKafkaProperties {

    private final String bootstrapServers;
    private final String schemaRegistryUrl;
    private final String username;
    private final String password;
    private final Topic topic;
    private final String applicationId;
    private final String trustStorePath;
    private final String trustStorePassword;

    @Inject
    VedtakStreamKafkaProperties(@KonfigVerdi("kafka.bootstrap.servers") String bootstrapServers,
                                @KonfigVerdi("kafka.schema.registry.url") String schemaRegistryUrl,
                                @KonfigVerdi("systembruker.username") String username,
                                @KonfigVerdi("systembruker.password") String password,
                                @KonfigVerdi(value = "javax.net.ssl.trustStore", required = false) String trustStorePath,
                                @KonfigVerdi(value = "javax.net.ssl.trustStorePassword", required = false) String trustStorePassword) {
        this.trustStorePath = trustStorePath;
        this.trustStorePassword = trustStorePassword;
        this.topic = TopicManifest.FATTET_VEDTAK;
        this.applicationId = ApplicationIdUtil.get();
        this.bootstrapServers = bootstrapServers;
        this.schemaRegistryUrl = schemaRegistryUrl;
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
        return topic.getConsumerClientId();
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

    @SuppressWarnings("resource")
    Class<?> getKeyClass() {
        return topic.getSerdeKey().getClass();
    }

    @SuppressWarnings("resource")
    Class<?> getValueClass() {
        return topic.getSerdeValue().getClass();
    }

    boolean harSattBrukernavn() {
        return username != null && !username.isEmpty();
    }

    String getApplicationId() {
        return applicationId;
    }

    boolean harSattTrustStore() {
        return trustStorePath != null && !trustStorePath.isEmpty()
            && trustStorePassword != null && !trustStorePassword.isEmpty();
    }

    String getTrustStorePath() {
        return trustStorePath;
    }

    String getTrustStorePassword() {
        return trustStorePassword;
    }
}
