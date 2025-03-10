package no.nav.foreldrepenger.abakus.rydding.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.abakus.rydding.OppryddingIayAggregatRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
@ProsessTask(value = "opprydding.iayGrunnlag.uten.referanse", maxFailedRuns = 2)
public class FjernIayGrunnlagUtenReferanseTask implements ProsessTaskHandler {

    public static final int IAY_GRUNNLAG_BATCH_SIZE = 500;

    private static final Logger LOG = LoggerFactory.getLogger(FjernIayGrunnlagUtenReferanseTask.class);

    private final OppryddingIayAggregatRepository iayAggregatRepository;
    private final ProsessTaskTjeneste taskTjeneste;

    @Inject
    public FjernIayGrunnlagUtenReferanseTask(OppryddingIayAggregatRepository iayAggregatRepository, ProsessTaskTjeneste taskTjeneste) {
        this.iayAggregatRepository = iayAggregatRepository;
        this.taskTjeneste = taskTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData unused) {
        var iayAggregatUtenReferanse = iayAggregatRepository.hentIayAggregaterUtenReferanse(IAY_GRUNNLAG_BATCH_SIZE);
        LOG.info("Fjerner {} IAY-aggregater uten referanse.", iayAggregatUtenReferanse.size());
        iayAggregatUtenReferanse.forEach(iayAggregatRepository::slettIayAggregat);
        LOG.info("Slettet {} IAY-aggregater uten referanse", iayAggregatUtenReferanse.size());

        if (iayAggregatUtenReferanse.size() >= IAY_GRUNNLAG_BATCH_SIZE) {
            opprettFjernIayAggregatTask();
        }
    }

    private void opprettFjernIayAggregatTask() {
        LOG.info("Oppretter en ny task for å fjerne IAY-aggregater uten referanse.");
        var prosessTaskData = ProsessTaskData.forProsessTask(FjernIayGrunnlagUtenReferanseTask.class);
        taskTjeneste.lagre(prosessTaskData);
    }
}
