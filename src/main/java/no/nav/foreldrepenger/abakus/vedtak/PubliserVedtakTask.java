package no.nav.foreldrepenger.abakus.vedtak;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.abakus.aktor.AktørTjeneste;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.kafka.KafkaSender;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ApplicationScoped
@ProsessTask(value = "vedtakEvent.publiser", prioritet = 2)
public class PubliserVedtakTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(PubliserVedtakTask.class);

    public static final String KEY = "publiser.key";
    public static final String TEMA = "publiser.tema";
    public static final String IDENT = "publiser.ident";
    public static final String AKTØRID = "publiser.aktorid";

    private AktørTjeneste aktørTjeneste;
    private KafkaSender producer;

    public PubliserVedtakTask() {
    }

    @Inject
    public PubliserVedtakTask(AktørTjeneste aktørTjeneste, @KonfigVerdi(value = "kafka.eksternvedtak.topic") String topicName) {
        this.aktørTjeneste = aktørTjeneste;
        this.producer = new KafkaSender(topicName);
    }

    @Override
    public void doTask(ProsessTaskData data) {
        var key = Objects.requireNonNull(data.getPropertyValue(KEY));
        var tema = Objects.requireNonNull(data.getPropertyValue(TEMA));
        var identKey = data.getPropertyValue(IDENT);
        var aktørId = data.getPropertyValue(AKTØRID);

        var ident = Optional.ofNullable(aktørId).map(AktørId::new)
            .flatMap(a -> aktørTjeneste.hentIdentForAktør(a))
            .map(PersonIdent::getIdent)
            .or(() -> Optional.ofNullable(identKey));
        if (ident.isPresent()) {
            LOG.info("Sender melding om vedtak med nøkkel {} på topic='{}'", key, producer.getTopicName());
            var vedtak = new EksternVedtak(ident.get(), OffsetDateTime.now(), tema);
            var recordMetadata = producer.send(key, DefaultJsonMapper.toJson(vedtak));
            LOG.info("Sendte melding om vedtak til {}, partition {}, offset {}", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
        } else {
            LOG.warn("Mangler ident for hendelse med nøkkel {}", key);
        }
    }

    public record EksternVedtak(String personidentifikator, OffsetDateTime tidspunkt, String tema) {}
}
