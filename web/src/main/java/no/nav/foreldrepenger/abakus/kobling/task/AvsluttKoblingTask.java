package no.nav.foreldrepenger.abakus.kobling.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.KoblingTask;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.kobling.TaskConstants;
import no.nav.foreldrepenger.abakus.kobling.repository.LåsRepository;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ApplicationScoped
@ProsessTask("kobling.avslutt")
public class AvsluttKoblingTask extends KoblingTask {
    private static final Logger LOG = LoggerFactory.getLogger(AvsluttKoblingTask.class);

    private static final Environment ENV = Environment.current();

    private KoblingTjeneste koblingTjeneste;
    private InntektArbeidYtelseTjeneste iayTjeneste;

    AvsluttKoblingTask() {
        // CDI
    }

    @Inject
    public AvsluttKoblingTask(LåsRepository låsRepository, KoblingTjeneste koblingTjeneste, InntektArbeidYtelseTjeneste iayTjeneste) {
        super(låsRepository);
        this.koblingTjeneste = koblingTjeneste;
        this.iayTjeneste = iayTjeneste;
    }

    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        var koblingId = Long.valueOf(prosessTaskData.getPropertyValue(TaskConstants.KOBLING_ID));
        // Midlertidig for å kunne fikse ytelse-type UNDEFINED.
        var ytelseType = YtelseType.fraKode(prosessTaskData.getPropertyValue(TaskConstants.YTELSE_TYPE));

        var kobling = koblingTjeneste.hent(koblingId);
        // Vi fikser manglende ytelseType på kobling
        if (YtelseType.UDEFINERT.equals(kobling.getYtelseType()) && ytelseType != null) {
            kobling.setYtelseType(ytelseType);
        }

        LOG.info("Starter avslutting av kobling for sak=[{}, {}] med behandling='{}'", kobling.getSaksnummer(), kobling.getYtelseType(),
            kobling.getKoblingReferanse());

        if (!ENV.isProd()) {
            iayTjeneste.slettInaktiveGrunnlagFor(kobling.getKoblingReferanse());
        }

        kobling.setAktiv(false);

        LOG.info("Ferdig med avlutting av kobling for sak=[{}, {}] med behandling='{}'",
            kobling.getSaksnummer(), kobling.getYtelseType(), kobling.getKoblingReferanse());
    }
}
