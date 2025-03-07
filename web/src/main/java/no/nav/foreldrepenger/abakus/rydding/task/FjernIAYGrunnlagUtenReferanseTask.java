package no.nav.foreldrepenger.abakus.rydding.task;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.abakus.rydding.OppryddingIAYAggregatRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ApplicationScoped
@ProsessTask(value = "opprydding.iayGrunnlag.uten.referanse", maxFailedRuns = 2)
public class FjernIAYGrunnlagUtenReferanseTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(FjernIAYGrunnlagUtenReferanseTask.class);
    private final OppryddingIAYAggregatRepository iayAggregatRepository;

    @Inject
    public FjernIAYGrunnlagUtenReferanseTask(OppryddingIAYAggregatRepository iayAggregatRepository) {
        this.iayAggregatRepository = iayAggregatRepository;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Set<Integer> iayAggregatUtenReferanse = DefaultJsonMapper.fromJson(prosessTaskData.getPayloadAsString(), Set.class);
        LOG.info("Fjerner {} IAY-aggregater uten referanse.", iayAggregatUtenReferanse.size());
        iayAggregatUtenReferanse.forEach(iayId ->  iayAggregatRepository.slettIayAggregat(iayId.longValue()));
        LOG.info("Slettet {} IAY-aggregater uten referanse", iayAggregatUtenReferanse.size());
    }
}
