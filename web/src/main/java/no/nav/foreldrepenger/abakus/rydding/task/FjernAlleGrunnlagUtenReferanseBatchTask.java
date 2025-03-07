package no.nav.foreldrepenger.abakus.rydding.task;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.abakus.rydding.OppryddingTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(value = "opprydding.grunnlag.uten.referanse", cronExpression = "0 0 3 * * *", maxFailedRuns = 1)
public class FjernAlleGrunnlagUtenReferanseBatchTask implements ProsessTaskHandler {

    private final OppryddingTjeneste oppryddingTjeneste;

    @Inject
    public FjernAlleGrunnlagUtenReferanseBatchTask(OppryddingTjeneste oppryddingTjeneste) {
        this.oppryddingTjeneste = oppryddingTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        oppryddingTjeneste.fjernAlleIayAggregatUtenReferanse();
    }
}
