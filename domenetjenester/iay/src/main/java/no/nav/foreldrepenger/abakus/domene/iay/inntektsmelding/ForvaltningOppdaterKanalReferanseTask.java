package no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.hibernate.jpa.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.integrasjon.saf.SafTjeneste;
import no.nav.vedtak.felles.integrasjon.saf.graphql.JournalpostQuery;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(ForvaltningOppdaterKanalReferanseTask.TASK_TYPE)
public class ForvaltningOppdaterKanalReferanseTask implements ProsessTaskHandler {
    private static final Logger log = LoggerFactory.getLogger(ForvaltningOppdaterKanalReferanseTask.class);
    public static final String TASK_TYPE = "forvaltning.oppdaterKanalreferanse";
    private EntityManager entityManager;
    private SafTjeneste safTjeneste;

    public ForvaltningOppdaterKanalReferanseTask() {
        // proxy
    }

    @Inject
    public ForvaltningOppdaterKanalReferanseTask(SafTjeneste safTjeneste, EntityManager entityManager) {
        this.safTjeneste = safTjeneste;
        this.entityManager = entityManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var max = Integer.valueOf(prosessTaskData.getProperties().getProperty("max.antall", "5"));

        Stream<String> stream = entityManager.createNativeQuery("select distinct journalpost_id from IAY_INNTEKTSMELDING where kanalreferanse is null")
            .setMaxResults(max)
            .setHint(QueryHints.HINT_FETCH_SIZE, max)
            .getResultStream();

        var journalpostIder = stream.limit(max).collect(Collectors.toList());

        if (journalpostIder.isEmpty()) {
            log.warn("Ingen journalpost ider uten kanalreferanse funnet. Forvaltningsoppgaven kan avsluttes.");
            return;
        }

        for (var jourId : journalpostIder) {
            JournalpostQuery query = new JournalpostQuery(jourId);
            var journalpostInfo = safTjeneste.hentJournalpostInfo(query);

            String kanalreferanse = journalpostInfo.getEksternReferanseId();

            if ("INGEN".equals(kanalreferanse)) {
                log.warn("Fant dodgy kanalreferanse {} for journalpost: {}", kanalreferanse, jourId);
            } else if (kanalreferanse != null) {
                log.info("Fant kanalreferanse {} for journalpost {}", kanalreferanse, jourId);
                entityManager.createNativeQuery("update IAY_INNTEKTSMELDING set kanalreferanse=:kanalreferanse where journalpost_id=:journalpostId AND kanalreferanse IS NULL")
                    .setParameter("kanalreferanse", kanalreferanse)
                    .setParameter("journalpostId", jourId)
                    .executeUpdate();
            } else {
                log.warn("Fant ikke kanalreferanse for journalpost: {}", jourId);
            }
        }

    }
}
