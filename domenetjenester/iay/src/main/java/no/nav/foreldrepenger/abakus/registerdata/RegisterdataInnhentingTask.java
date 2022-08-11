package no.nav.foreldrepenger.abakus.registerdata;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.abakus.iaygrunnlag.JsonObjectMapper;
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
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ApplicationScoped
@ProsessTask("registerdata.innhent")
public class RegisterdataInnhentingTask extends KoblingTask {

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
        log.info("Starter registerinnhenting for sak=[{}, {}] med behandling='{}'", kobling.getSaksnummer(), kobling.getYtelseType(), kobling.getKoblingReferanse());

        Set<RegisterdataElement> informasjonsElementer;
        var payloadAsString = prosessTaskData.getPayloadAsString();
        if (payloadAsString != null && !payloadAsString.isEmpty()) {
            try {
                var request = JsonObjectMapper.getMapper().readValue(payloadAsString, InnhentRegisterdataRequest.class);
                informasjonsElementer = InnhentRegisterdataTjeneste.hentUtInformasjonsElementer(request);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Feilet i deserialisering av innhent request", e);
            }
        } else {
            informasjonsElementer = Set.of(RegisterdataElement.values());
        }
        log.info("Registerdataelementer for sak=[{}, {}] med behandling='{}' er: {} ", kobling.getSaksnummer(), kobling.getYtelseType(), kobling.getKoblingReferanse(), informasjonsElementer);
        InntektArbeidYtelseGrunnlagBuilder builder = finnInnhenter(kobling.getYtelseType()).innhentRegisterdata(kobling, informasjonsElementer);
        iayTjeneste.lagre(kobling.getKoblingReferanse(), builder);
    }
}
