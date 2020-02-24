package no.nav.foreldrepenger.abakus.registerdata.callback;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.callback.registerdata.CallbackDto;
import no.nav.abakus.callback.registerdata.Grunnlag;
import no.nav.abakus.callback.registerdata.ReferanseDto;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.kobling.TaskConstants;
import no.nav.foreldrepenger.abakus.registerdata.InnhentingFeil;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(CallbackTask.TASKTYPE)
public class CallbackTask implements ProsessTaskHandler {
    public static final String TASKTYPE = "registerdata.callback";
    public static final String EKSISTERENDE_GRUNNLAG_REF = "grunnlag.ref.old";
    private static final Logger log = LoggerFactory.getLogger(CallbackTask.class);

    private OidcRestClient restClient;
    private KoblingTjeneste koblingTjeneste;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;

    CallbackTask() {
    }

    @Inject
    public CallbackTask(OidcRestClient oidcRestClient,
                        KoblingTjeneste koblingTjeneste,
                        InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste) {
        this.restClient = oidcRestClient;
        this.koblingTjeneste = koblingTjeneste;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData data) {
        String callbackUrl = data.getPropertyValue(TaskConstants.CALLBACK_URL);
        Kobling kobling = koblingTjeneste.hent(data.getBehandlingId());
        if (callbackUrl == null || callbackUrl.isEmpty()) {
            log.info("Prøver callback uten url for kobling: {} ... Ignorerer", kobling);
            return;
        }
        CallbackDto callbackDto = new CallbackDto();
        callbackDto.setGrunnlagType(Grunnlag.IAY);

        setInformasjonOmAvsenderRef(kobling, callbackDto);
        setInformasjonOmEksisterendeGrunnlag(data, callbackDto);
        setInformasjonOmNyttGrunnlag(kobling, data, callbackDto);

        URI uri;
        try {
            uri = new URI(callbackUrl);
        } catch (URISyntaxException e) {
            throw InnhentingFeil.FACTORY.ugyldigCallbackUrl(callbackUrl).toException();
        }
        String post = restClient.post(uri, callbackDto);

        log.info("Callback success, mottok respons: " + post);
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
        Optional<InntektArbeidYtelseGrunnlag> grunnlag = inntektArbeidYtelseTjeneste.hentGrunnlagFor(kobling.getKoblingReferanse());
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
