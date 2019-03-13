package no.nav.foreldrepenger.abakus.registerdata;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingTask;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.kobling.repository.LåsRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ApplicationScoped
@ProsessTask(RegisterdataInnhentingTask.TASKTYPE)
public class RegisterdataInnhentingTask extends KoblingTask {
    public static final String TASKTYPE = "registerdata.innhent";

    private KoblingTjeneste koblingTjeneste;
    private InntektArbeidYtelseTjeneste iayTjeneste;
    private IAYRegisterInnhentingTjeneste registerInnhentingTjeneste;

    RegisterdataInnhentingTask() {
    }

    @Inject
    public RegisterdataInnhentingTask(LåsRepository låsRepository,
                                      KoblingTjeneste koblingTjeneste,
                                      InntektArbeidYtelseTjeneste iayTjeneste,
                                      @FagsakYtelseTypeRef("FP") IAYRegisterInnhentingTjeneste registerInnhentingTjeneste) {
        super(låsRepository);
        this.koblingTjeneste = koblingTjeneste;
        this.iayTjeneste = iayTjeneste;
        this.registerInnhentingTjeneste = registerInnhentingTjeneste;
    }


    @Override
    protected void prosesser(ProsessTaskData prosessTaskData) {
        Kobling kobling = koblingTjeneste.hent(prosessTaskData.getKoblingId());

        InntektArbeidYtelseAggregatBuilder builder = registerInnhentingTjeneste.innhentRegisterdata(kobling);
        iayTjeneste.lagre(kobling.getId(), builder);
    }
}
