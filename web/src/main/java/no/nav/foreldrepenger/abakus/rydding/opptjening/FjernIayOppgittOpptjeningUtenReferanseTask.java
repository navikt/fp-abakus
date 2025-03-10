package no.nav.foreldrepenger.abakus.rydding.opptjening;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
@ProsessTask(value = "opprydding.iayOppgittOpptjening.uten.referanse", maxFailedRuns = 2)
public class FjernIayOppgittOpptjeningUtenReferanseTask implements ProsessTaskHandler {
    static final int IAY_OPPGITT_OPPTJENING_BATCH_SIZE = 500;

    private static final Logger LOG = LoggerFactory.getLogger(FjernIayOppgittOpptjeningUtenReferanseTask.class);

    private final OppryddingIayOppgittOpptjeningRepository oppryddingIayOppgittOpptjeningRepository;
    private final ProsessTaskTjeneste taskTjeneste;

    @Inject
    public FjernIayOppgittOpptjeningUtenReferanseTask(OppryddingIayOppgittOpptjeningRepository oppryddingIayOppgittOpptjeningRepository,
                                                      ProsessTaskTjeneste taskTjeneste) {
        this.oppryddingIayOppgittOpptjeningRepository = oppryddingIayOppgittOpptjeningRepository;
        this.taskTjeneste = taskTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData unused) {
        var iayOppgittOpptjeningUtenReferanse = oppryddingIayOppgittOpptjeningRepository.hentIayOppgittOpptjeningUtenReferanse(
            IAY_OPPGITT_OPPTJENING_BATCH_SIZE);
        LOG.info("Fjerner {} IAY-Oppgitt Opptjening uten referanse.", iayOppgittOpptjeningUtenReferanse.size());
        iayOppgittOpptjeningUtenReferanse.forEach(oppryddingIayOppgittOpptjeningRepository::slettIayOppgittOpptjening);
        LOG.info("Slettet {} IAY-Oppgitt Opptjening uten referanse", iayOppgittOpptjeningUtenReferanse.size());

        if (iayOppgittOpptjeningUtenReferanse.size() >= IAY_OPPGITT_OPPTJENING_BATCH_SIZE) {
            opprettFjernOppgittOpptjeningAggregatTask();
        }
    }

    private void opprettFjernOppgittOpptjeningAggregatTask() {
        LOG.info("Oppretter en ny task for å fjerne IAY-Oppgitt Opptjening uten referanse.");
        var prosessTaskData = ProsessTaskData.forProsessTask(FjernIayOppgittOpptjeningUtenReferanseTask.class);
        taskTjeneste.lagre(prosessTaskData);
    }
}
