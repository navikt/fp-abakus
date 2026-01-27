package no.nav.foreldrepenger.abakus.registerdata;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.abakus.iaygrunnlag.request.InnhentRegisterdataRequest;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingTask;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.kobling.TaskConstants;
import no.nav.foreldrepenger.abakus.kobling.repository.LåsRepository;
import no.nav.foreldrepenger.abakus.registerdata.tjeneste.InnhentRegisterdataTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.tjeneste.RegisterdataElement;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ApplicationScoped
@ProsessTask("registerdata.innhent")
public class RegisterdataInnhentingTask extends KoblingTask {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterdataInnhentingTask.class);
    private KoblingTjeneste koblingTjeneste;
    private InntektArbeidYtelseTjeneste iayTjeneste;
    private IAYRegisterInnhentingTjeneste innhentTjeneste;

    RegisterdataInnhentingTask() {
        // CDI proxy
    }

    @Inject
    public RegisterdataInnhentingTask(LåsRepository låsRepository,
                                      KoblingTjeneste koblingTjeneste,
                                      InntektArbeidYtelseTjeneste iayTjeneste,
                                      IAYRegisterInnhentingTjeneste innhentingTjeneste) {
        super(låsRepository);
        this.koblingTjeneste = koblingTjeneste;
        this.iayTjeneste = iayTjeneste;
        this.innhentTjeneste = innhentingTjeneste;
    }

    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        Long koblingId = Long.valueOf(prosessTaskData.getPropertyValue(TaskConstants.KOBLING_ID));
        Kobling kobling = koblingTjeneste.hent(koblingId);
        LOG.info("Starter registerinnhenting for sak=[{}, {}] med behandling='{}'", kobling.getSaksnummer(), kobling.getYtelseType(),
            kobling.getKoblingReferanse());

        Set<RegisterdataElement> informasjonsElementer;
        var payloadAsString = prosessTaskData.getPayloadAsString();
        if (payloadAsString != null && !payloadAsString.isEmpty()) {
            var request = DefaultJsonMapper.fromJson(payloadAsString, InnhentRegisterdataRequest.class);
            informasjonsElementer = InnhentRegisterdataTjeneste.hentUtInformasjonsElementer(request);
        } else {
            informasjonsElementer = Set.of(RegisterdataElement.values());
        }
        LOG.info("Registerdataelementer for sak=[{}, {}] med behandling='{}' er: {} ", kobling.getSaksnummer(), kobling.getYtelseType(),
            kobling.getKoblingReferanse(), informasjonsElementer);
        InntektArbeidYtelseGrunnlagBuilder builder = innhentTjeneste.innhentRegisterdata(kobling, informasjonsElementer);
        iayTjeneste.lagre(kobling.getKoblingReferanse(), builder);
    }

}
