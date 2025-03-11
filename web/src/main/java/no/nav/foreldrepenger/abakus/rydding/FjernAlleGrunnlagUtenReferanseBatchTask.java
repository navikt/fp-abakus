package no.nav.foreldrepenger.abakus.rydding;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
// Kjører kl 02:00 hver Søndag
@ProsessTask(value = "opprydding.grunnlag.uten.referanse", cronExpression = "0 0 2 * * 0", maxFailedRuns = 1)
public class FjernAlleGrunnlagUtenReferanseBatchTask implements ProsessTaskHandler {

    private final OppryddingTjeneste oppryddingTjeneste;

    @Inject
    public FjernAlleGrunnlagUtenReferanseBatchTask(OppryddingTjeneste oppryddingTjeneste) {
        this.oppryddingTjeneste = oppryddingTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        oppryddingTjeneste.fjernAlleInaktiveAggregaterUtenReferanse();
    }
}
