package no.nav.foreldrepenger.abakus.rydding.inntektsmelding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
@ProsessTask(value = "opprydding.iayInntektsmelding.uten.referanse", maxFailedRuns = 2)
public class FjernIayInntektsmeldingerUtenReferanseTask implements ProsessTaskHandler {
    static final int IAY_INNTEKTSMELDING_BATCH_SIZE = 500;

    private static final Logger LOG = LoggerFactory.getLogger(FjernIayInntektsmeldingerUtenReferanseTask.class);

    private final OppryddingIayInntektsmeldingerRepository oppryddingIayInntektsmeldingerRepository;
    private final ProsessTaskTjeneste taskTjeneste;

    @Inject
    public FjernIayInntektsmeldingerUtenReferanseTask(OppryddingIayInntektsmeldingerRepository oppryddingIayInntektsmeldingerRepository,
                                                      ProsessTaskTjeneste taskTjeneste) {
        this.oppryddingIayInntektsmeldingerRepository = oppryddingIayInntektsmeldingerRepository;
        this.taskTjeneste = taskTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData unused) {
        var iayInntektsmeldingerUtenReferanse = oppryddingIayInntektsmeldingerRepository.hentIayInntektsmeldingerUtenReferanse(
            IAY_INNTEKTSMELDING_BATCH_SIZE);
        LOG.info("Fjerner {} IAY-Inntektsmeldinger uten referanse.", iayInntektsmeldingerUtenReferanse.size());
        iayInntektsmeldingerUtenReferanse.forEach(oppryddingIayInntektsmeldingerRepository::slettIayInntektsmeldinger);
        LOG.info("Slettet {} IAY-Inntektsmeldinger uten referanse", iayInntektsmeldingerUtenReferanse.size());

        if (iayInntektsmeldingerUtenReferanse.size() >= IAY_INNTEKTSMELDING_BATCH_SIZE) {
            opprettFjernInntektsmeldingAggregatTask();
        }
    }

    private void opprettFjernInntektsmeldingAggregatTask() {
        LOG.info("Oppretter en ny task for å fjerne IAY-Inntektsmeldinger uten referanse.");
        var prosessTaskData = ProsessTaskData.forProsessTask(FjernIayInntektsmeldingerUtenReferanseTask.class);
        taskTjeneste.lagre(prosessTaskData);
    }
}
