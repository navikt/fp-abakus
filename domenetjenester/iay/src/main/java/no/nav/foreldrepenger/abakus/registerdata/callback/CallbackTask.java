package no.nav.foreldrepenger.abakus.registerdata.callback;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.abakus.callback.registerdata.CallbackDto;
import no.nav.abakus.callback.registerdata.Grunnlag;
import no.nav.abakus.callback.registerdata.ReferanseDto;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.kobling.TaskConstants;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask("registerdata.callback")
public class CallbackTask implements ProsessTaskHandler {

    public static final String EKSISTERENDE_GRUNNLAG_REF = "grunnlag.ref.old";

    private FpsakKlient fpsakKlient;
    private KoblingTjeneste koblingTjeneste;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;

    CallbackTask() {
    }

    @Inject
    public CallbackTask(FpsakKlient fpsakKlient, KoblingTjeneste koblingTjeneste, InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste) {
        this.fpsakKlient = fpsakKlient;
        this.koblingTjeneste = koblingTjeneste;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData data) {
        String nyKoblingId = data.getPropertyValue(TaskConstants.KOBLING_ID);
        Long koblingId = nyKoblingId != null ? Long.valueOf(nyKoblingId) : data.getBehandlingIdAsLong();
        Kobling kobling = koblingTjeneste.hent(koblingId);

        CallbackDto callbackDto = new CallbackDto();

        setInformasjonOmAvsenderRef(kobling, callbackDto);
        setInformasjonOmEksisterendeGrunnlag(data, callbackDto);
        setInformasjonOmNyttGrunnlag(kobling, data, callbackDto);

        fpsakKlient.sendCallback(callbackDto);
    }

    private void setInformasjonOmAvsenderRef(Kobling kobling, CallbackDto callbackDto) {
        UUID koblingReferanse = kobling.getKoblingReferanse().getReferanse();
        ReferanseDto avsenderRef = new ReferanseDto();
        avsenderRef.setReferanse(koblingReferanse);
        callbackDto.setAvsenderRef(avsenderRef);
    }

    private void setInformasjonOmEksisterendeGrunnlag(ProsessTaskData data, CallbackDto callbackDto) {
        String eksisterendeGrunnlagRef = data.getPropertyValue(EKSISTERENDE_GRUNNLAG_REF);
        if (eksisterendeGrunnlagRef != null && !eksisterendeGrunnlagRef.isEmpty()) {
            ReferanseDto eksisterendeRef = new ReferanseDto();
            eksisterendeRef.setReferanse(UUID.fromString(eksisterendeGrunnlagRef));
            callbackDto.setOpprinneligGrunnlagRef(eksisterendeRef);
        }
    }

    private void setInformasjonOmNyttGrunnlag(Kobling kobling, ProsessTaskData data, CallbackDto callbackDto) {
        var grunnlag = inntektArbeidYtelseTjeneste.hentGrunnlagFor(kobling.getKoblingReferanse());
        grunnlag.ifPresent(gr -> {
            ReferanseDto grunnlagRef = new ReferanseDto();
            grunnlagRef.setReferanse(gr.getGrunnlagReferanse().getReferanse());
            callbackDto.setOppdatertGrunnlagRef(grunnlagRef);
            callbackDto.setOpprettetTidspunkt(gr.getOpprettetTidspunkt());
        });
        if (grunnlag.isEmpty()) {
            callbackDto.setOpprettetTidspunkt(data.getSistKjørt());
        }
    }

}
