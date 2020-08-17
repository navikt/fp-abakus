package no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;

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
        var max = Integer.valueOf(prosessTaskData.getProperties().getProperty("max.antall", "10"));

        Stream<Tuple> stream = entityManager.createNativeQuery("select distinct journalpost_id, endret_tid from IAY_INNTEKTSMELDING where kanalreferanse is null order by endret_tid nulls first", Tuple.class)
            .setMaxResults(max)
            .setHint(QueryHints.HINT_FETCH_SIZE, max)
            .getResultStream();

        var journalpostIder = stream.map(t -> t.get("journalpost_id", String.class)).limit(max).collect(Collectors.toList());


        boolean funnet = false;
        for (var jourId : journalpostIder) {
            JournalpostQuery query = new JournalpostQuery(jourId);
            var journalpostInfo = safTjeneste.hentJournalpostInfo(query);

            String kanalreferanse = journalpostInfo.getEksternReferanseId();

            if (kanalreferanse == null || "INGEN".equals(kanalreferanse)) {
                log.warn("Fant dodgy kanalreferanse {} for journalpost: {}", kanalreferanse, jourId);
                kanalreferanse = null;
            } else {
                funnet = true;
                log.info("Fant kanalreferanse {} for journalpost {}", kanalreferanse, jourId);
            }
            entityManager.createNativeQuery("update IAY_INNTEKTSMELDING"
                + " set kanalreferanse=:kanalreferanse, endret_tid=current_timestamp at time zone 'UTC' "
                + "where journalpost_id=:journalpostId AND kanalreferanse IS NULL")
                .setParameter("kanalreferanse", kanalreferanse)
                .setParameter("journalpostId", jourId)
                .executeUpdate();
        }
        
        if (!funnet) {
            log.warn("Journalpostider ({}) uten kanalreferanse funnet, eller alle var null. Forvaltningsoppgaven kan avsluttes.", journalpostIder.size());
            return;
        }
        

    }
}
