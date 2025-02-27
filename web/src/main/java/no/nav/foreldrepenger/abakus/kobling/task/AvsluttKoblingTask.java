package no.nav.foreldrepenger.abakus.kobling.task;

import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.abakus.kobling.KoblingTask;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.kobling.TaskConstants;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ApplicationScoped
@ProsessTask("kobling.avslutt")
public class AvsluttKoblingTask extends KoblingTask {
    private static final Logger LOG = LoggerFactory.getLogger(AvsluttKoblingTask.class);

    private KoblingTjeneste koblingTjeneste;
    private InntektArbeidYtelseTjeneste iayTjeneste;

    AvsluttKoblingTask() {
        // CDI
    }

    @Inject
    public AvsluttKoblingTask(KoblingTjeneste koblingTjeneste, InntektArbeidYtelseTjeneste iayTjeneste) {
        this.koblingTjeneste = koblingTjeneste;
        this.iayTjeneste = iayTjeneste;
    }

    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        var koblingId = Long.valueOf(prosessTaskData.getPropertyValue(TaskConstants.KOBLING_ID));
        var kobling = koblingTjeneste.hent(koblingId);

        LOG.info("Starter avslutting av kobling for sak=[{}, {}] med behandling='{}'", kobling.getSaksnummer(), kobling.getYtelseType(),
            kobling.getKoblingReferanse());



        koblingTjeneste.deaktiver(kobling.getKoblingReferanse());

        LOG.info("Ferdig med avlutting av kobling for sak=[{}, {}] med behandling='{}' fjernet følgende inaktive grunnlag: {} ",
            kobling.getSaksnummer(), kobling.getYtelseType(), kobling.getKoblingReferanse());
    }
}
