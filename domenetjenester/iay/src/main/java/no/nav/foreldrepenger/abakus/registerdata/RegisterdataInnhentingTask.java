package no.nav.foreldrepenger.abakus.registerdata;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.request.InnhentRegisterdataRequest;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingTask;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.kobling.kontroll.YtelseTypeRef;
import no.nav.foreldrepenger.abakus.kobling.repository.LåsRepository;
import no.nav.foreldrepenger.abakus.registerdata.tjeneste.InnhentRegisterdataTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.tjeneste.RegisterdataElement;
import no.nav.foreldrepenger.abakus.vedtak.json.JacksonJsonConfig;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ApplicationScoped
@ProsessTask(RegisterdataInnhentingTask.TASKTYPE)
public class RegisterdataInnhentingTask extends KoblingTask {

    public static final String TASKTYPE = "registerdata.innhent";
    private static final Logger log = LoggerFactory.getLogger(RegisterdataInnhentingTask.class);
    private KoblingTjeneste koblingTjeneste;
    private InntektArbeidYtelseTjeneste iayTjeneste;
    private Instance<IAYRegisterInnhentingTjeneste> innhentTjenester;

    RegisterdataInnhentingTask() {
    }

    @Inject
    public RegisterdataInnhentingTask(LåsRepository låsRepository,
                                      KoblingTjeneste koblingTjeneste,
                                      InntektArbeidYtelseTjeneste iayTjeneste,
                                      @Any Instance<IAYRegisterInnhentingTjeneste> innhentingTjeneste) {
        super(låsRepository);
        this.koblingTjeneste = koblingTjeneste;
        this.iayTjeneste = iayTjeneste;
        this.innhentTjenester = innhentingTjeneste;
    }

    private IAYRegisterInnhentingTjeneste finnInnhenter(YtelseType ytelseType) {
        return YtelseTypeRef.Lookup.find(innhentTjenester, ytelseType).get();
    }

    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        Kobling kobling = koblingTjeneste.hent(Long.valueOf(prosessTaskData.getBehandlingId()));

        Set<RegisterdataElement> informasjonsElementer;
        log.info("Starter registerinnhenting for sak=[{}, {}] med behandling='{}'", kobling.getSaksnummer(), kobling.getYtelseType(), kobling.getKoblingReferanse());
        try {
            var request = JacksonJsonConfig.getMapper().readValue(prosessTaskData.getPayloadAsString(), InnhentRegisterdataRequest.class);
            informasjonsElementer = InnhentRegisterdataTjeneste.hentUtInformasjonsElementer(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Feilet i deserialisering av innhent request", e);
        }

        InntektArbeidYtelseGrunnlagBuilder builder = finnInnhenter(kobling.getYtelseType()).innhentRegisterdata(kobling, informasjonsElementer);
        iayTjeneste.lagre(kobling.getKoblingReferanse(), builder);
    }
}
