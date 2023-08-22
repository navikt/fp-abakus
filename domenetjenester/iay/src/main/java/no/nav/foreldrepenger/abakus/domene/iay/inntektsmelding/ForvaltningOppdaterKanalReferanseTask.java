package no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(value = "forvaltning.oppdaterKanalreferanse", maxFailedRuns = 50)
public class ForvaltningOppdaterKanalReferanseTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ForvaltningOppdaterKanalReferanseTask.class);

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

        Stream<Tuple> stream = entityManager.createNativeQuery(
            "select distinct journalpost_id, endret_tid from IAY_INNTEKTSMELDING where kanalreferanse is null order by endret_tid nulls first",
            Tuple.class).getResultStream();

        var journalpostIder = stream.map(t -> t.get("journalpost_id", String.class)).collect(Collectors.toList());

        for (var jourId : journalpostIder) {
            LOG.info("Mangler kanalreferanse for journalpost [{}] ", jourId);
        }
        LOG.info("Mangler kanalreferanse for antall journalposter: ({})", journalpostIder.size());

    }
}
