package no.nav.foreldrepenger.abakus.rydding.task;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.abakus.rydding.OppryddingIayInformasjonRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@ProsessTask(value = "opprydding.iayInformasjon.uten.referanse", maxFailedRuns = 2)
public class FjernIayInformasjonUtenReferanseTask implements ProsessTaskHandler {
    public static final int IAY_ARBEIDSFORHOLD_INFORMASJON_BATCH_SIZE = 750;

    private static final Logger LOG = LoggerFactory.getLogger(FjernIayInformasjonUtenReferanseTask.class);

    private final OppryddingIayInformasjonRepository oppryddingIayInformasjonRepository;
    private final ProsessTaskTjeneste taskTjeneste;

    @Inject
    public FjernIayInformasjonUtenReferanseTask(OppryddingIayInformasjonRepository oppryddingIayInformasjonRepository, ProsessTaskTjeneste taskTjeneste) {
        this.oppryddingIayInformasjonRepository = oppryddingIayInformasjonRepository;
        this.taskTjeneste = taskTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData unused) {
        var iayInformasjonUtenReferanse = oppryddingIayInformasjonRepository.hentIayInformasjonUtenReferanse(IAY_ARBEIDSFORHOLD_INFORMASJON_BATCH_SIZE);
        LOG.info("Fjerner {} IAY-Informasjon uten referanse.", iayInformasjonUtenReferanse.size());
        iayInformasjonUtenReferanse.forEach(oppryddingIayInformasjonRepository::slettIayInformasjon);
        LOG.info("Slettet {} IAY-Informasjon uten referanse", iayInformasjonUtenReferanse.size());

        if (iayInformasjonUtenReferanse.size() >= IAY_ARBEIDSFORHOLD_INFORMASJON_BATCH_SIZE) {
            opprettFjernInformasjonAggregatTask();
        }
    }

    private void opprettFjernInformasjonAggregatTask() {
        LOG.info("Oppretter en ny task for å fjerne IAY-Informasjon uten referanse.");
        var prosessTaskData = ProsessTaskData.forProsessTask(FjernIayInformasjonUtenReferanseTask.class);
        taskTjeneste.lagre(prosessTaskData);
    }
}
