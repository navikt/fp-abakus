package no.nav.foreldrepenger.abakus.rydding;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.abakus.rydding.arbeidsforhold.FjernIayInformasjonUtenReferanseTask;
import no.nav.foreldrepenger.abakus.rydding.grunnlag.FjernIayGrunnlagUtenReferanseTask;
import no.nav.foreldrepenger.abakus.rydding.inntektsmelding.FjernIayInntektsmeldingerUtenReferanseTask;
import no.nav.foreldrepenger.abakus.rydding.opptjening.FjernIayOppgittOpptjeningUtenReferanseTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class OppryddingTjeneste {
    private static final Logger LOG = LoggerFactory.getLogger(OppryddingTjeneste.class);
    private ProsessTaskTjeneste taskTjeneste;

    OppryddingTjeneste() {
        // CDI proxy
    }

    @Inject
    public OppryddingTjeneste(ProsessTaskTjeneste taskTjeneste) {
        this.taskTjeneste = taskTjeneste;
    }

    /**
     * Fjerner alle inaktive aggregater uten referanse.
     * Oppretter fire tasker som sletter aggregater asynkront
     */
    public void fjernAlleInaktiveAggregaterUtenReferanse() {
        opprettFjernIayInntektArbeidYtelseAggregatTask();
        opprettFjernIayInformasjonTask();
        opprettFjernIayInntektsmeldingerTask();
        opprettFjernIayOppgittOpptjeningTask();
    }

    public void opprettFjernIayInntektArbeidYtelseAggregatTask() {
        LOG.info("Oppretter task for å fjerne IAY-aggregater uten referanse.");
        var prosessTaskData = ProsessTaskData.forProsessTask(FjernIayGrunnlagUtenReferanseTask.class);
        taskTjeneste.lagre(prosessTaskData);
    }

    public void opprettFjernIayInformasjonTask() {
        LOG.info("Oppretter task for å fjerne IAY-Informasjon uten referanse.");
        var prosessTaskData = ProsessTaskData.forProsessTask(FjernIayInformasjonUtenReferanseTask.class);
        taskTjeneste.lagre(prosessTaskData);
    }

    public void opprettFjernIayInntektsmeldingerTask() {
        LOG.info("Oppretter task for å fjerne IAY-Inntektsmeldinger uten referanse.");
        var prosessTaskData = ProsessTaskData.forProsessTask(FjernIayInntektsmeldingerUtenReferanseTask.class);
        taskTjeneste.lagre(prosessTaskData);
    }

    public void opprettFjernIayOppgittOpptjeningTask() {
        LOG.info("Oppretter task for å fjerne IAY-Oppgitt Opptjening uten referanse.");
        var prosessTaskData = ProsessTaskData.forProsessTask(FjernIayOppgittOpptjeningUtenReferanseTask.class);
        taskTjeneste.lagre(prosessTaskData);
    }
}
