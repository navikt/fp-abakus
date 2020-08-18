package no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(ForvaltningOppdaterKanalReferanseTask.TASK_TYPE)
public class ForvaltningOppdaterKanalReferanseTask implements ProsessTaskHandler {
    private static final Logger log = LoggerFactory.getLogger(ForvaltningOppdaterKanalReferanseTask.class);
    public static final String TASK_TYPE = "forvaltning.oppdaterKanalreferanse";
    private EntityManager entityManager;

    public ForvaltningOppdaterKanalReferanseTask() {
        // proxy
    }

    @Inject
    public ForvaltningOppdaterKanalReferanseTask(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void doTask(ProsessTaskData prosessTaskData) {

        Stream<Tuple> stream = entityManager.createNativeQuery("select distinct journalpost_id, endret_tid from IAY_INNTEKTSMELDING where kanalreferanse is null order by endret_tid nulls first", Tuple.class)
            .getResultStream();

        var journalpostIder = stream.map(t -> t.get("journalpost_id", String.class)).collect(Collectors.toList());

        for (var jourId : journalpostIder) {
            log.info("Mangler kanalreferanse for journalpost [{}] ", jourId);
        }
        log.info("Mangler kanalreferanse for antall journalposter: ({})", journalpostIder.size());

    }
}
