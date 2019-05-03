package no.nav.foreldrepenger.abakus.registerdata;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingTask;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.kobling.kontroll.YtelseTypeRef;
import no.nav.foreldrepenger.abakus.kobling.repository.LåsRepository;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ApplicationScoped
@ProsessTask(RegisterdataInnhentingTask.TASKTYPE)
public class RegisterdataInnhentingTask extends KoblingTask {
    public static final String TASKTYPE = "registerdata.innhent";

    private KodeverkRepository kodeverkRepository;
    private KoblingTjeneste koblingTjeneste;
    private InntektArbeidYtelseTjeneste iayTjeneste;
    private Map<YtelseType, IAYRegisterInnhentingTjeneste> registerInnhentingTjeneste;

    RegisterdataInnhentingTask() {
    }

    @Inject
    public RegisterdataInnhentingTask(LåsRepository låsRepository,
                                      KoblingTjeneste koblingTjeneste,
                                      InntektArbeidYtelseTjeneste iayTjeneste,
                                      KodeverkRepository kodeverkRepository,
                                      @Any Instance<IAYRegisterInnhentingTjeneste> innhentingTjeneste) {
        super(låsRepository);
        this.koblingTjeneste = koblingTjeneste;
        this.iayTjeneste = iayTjeneste;
        this.kodeverkRepository = kodeverkRepository;
        this.registerInnhentingTjeneste = new HashMap<>();
        innhentingTjeneste.forEach(innhenter -> populerMap(registerInnhentingTjeneste, innhenter));
    }

    private void populerMap(Map<YtelseType, IAYRegisterInnhentingTjeneste> map, IAYRegisterInnhentingTjeneste innhenter) {
        YtelseType type = YtelseType.UDEFINERT;
        if (innhenter.getClass().isAnnotationPresent(YtelseTypeRef.class)) {
            type = kodeverkRepository.finn(YtelseType.class, innhenter.getClass().getAnnotation(YtelseTypeRef.class).value());
        }
        map.put(type, innhenter);
    }

    private IAYRegisterInnhentingTjeneste finnInnhenter(YtelseType ytelseType) {
        IAYRegisterInnhentingTjeneste innhenter = registerInnhentingTjeneste.get(ytelseType);
        if (innhenter == null) {
            throw new IllegalArgumentException("Finner ikke IAYRegisterInnhenter. Støtter ikke ytelsetype " + ytelseType);
        }
        return innhenter;
    }

    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        Kobling kobling = koblingTjeneste.hent(prosessTaskData.getKoblingId());

        InntektArbeidYtelseAggregatBuilder builder = finnInnhenter(kobling.getYtelseType()).innhentRegisterdata(kobling);
        iayTjeneste.lagre(kobling.getId(), builder);
    }
}
