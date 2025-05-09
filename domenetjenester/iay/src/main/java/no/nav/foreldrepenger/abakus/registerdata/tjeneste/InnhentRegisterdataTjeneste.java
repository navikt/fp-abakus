package no.nav.foreldrepenger.abakus.registerdata.tjeneste;

import static no.nav.foreldrepenger.abakus.registerdata.callback.CallbackTask.EKSISTERENDE_GRUNNLAG_REF;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.abakus.iaygrunnlag.JsonObjectMapper;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.request.InnhentRegisterdataRequest;
import no.nav.abakus.iaygrunnlag.request.RegisterdataType;
import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.kobling.TaskConstants;
import no.nav.foreldrepenger.abakus.registerdata.RegisterdataInnhentingTask;
import no.nav.foreldrepenger.abakus.registerdata.callback.CallbackTask;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
public class InnhentRegisterdataTjeneste {

    private static final Map<RegisterdataType, RegisterdataElement> registerdataMapping = initMapping();
    private InntektArbeidYtelseTjeneste iayTjeneste;
    private KoblingTjeneste koblingTjeneste;
    private ProsessTaskTjeneste taskTjeneste;

    InnhentRegisterdataTjeneste() {
        // CDI
    }

    @Inject
    public InnhentRegisterdataTjeneste(InntektArbeidYtelseTjeneste iayTjeneste, KoblingTjeneste koblingTjeneste, ProsessTaskTjeneste taskTjeneste) {
        this.iayTjeneste = iayTjeneste;
        this.koblingTjeneste = koblingTjeneste;
        this.taskTjeneste = taskTjeneste;
    }

    private static Map<RegisterdataType, RegisterdataElement> initMapping() {
        return Map.of(RegisterdataType.ARBEIDSFORHOLD, RegisterdataElement.ARBEIDSFORHOLD, RegisterdataType.YTELSE, RegisterdataElement.YTELSE,
            RegisterdataType.LIGNET_NÆRING, RegisterdataElement.LIGNET_NÆRING, RegisterdataType.INNTEKT_PENSJONSGIVENDE,
            RegisterdataElement.INNTEKT_PENSJONSGIVENDE, RegisterdataType.INNTEKT_BEREGNINGSGRUNNLAG, RegisterdataElement.INNTEKT_BEREGNINGSGRUNNLAG,
            RegisterdataType.INNTEKT_SAMMENLIGNINGSGRUNNLAG, RegisterdataElement.INNTEKT_SAMMENLIGNINGSGRUNNLAG);
    }

    public static Set<RegisterdataElement> hentUtInformasjonsElementer(InnhentRegisterdataRequest dto) {
        final var elementer = dto.getElementer();

        if (elementer == null || elementer.isEmpty()) {
            return Set.of();
        }

        return elementer.stream().map(registerdataMapping::get).collect(Collectors.toSet());
    }

    private Kobling oppdaterKobling(InnhentRegisterdataRequest dto) {
        var referanse = new KoblingReferanse(dto.getReferanse());
        var koblingLås = Optional.ofNullable(koblingTjeneste.taSkrivesLås(referanse)); // kan bli null hvis gjelder ny
        var kobling = koblingTjeneste.hentFor(referanse)
            .orElse(new Kobling(dto.getYtelseType(), new Saksnummer(dto.getSaksnummer()), referanse, new AktørId(dto.getAktør().getIdent())));

        // Oppdater kobling med perioder
        mapPeriodeTilIntervall(dto.getOpplysningsperiode()).ifPresent(kobling::setOpplysningsperiode);
        mapPeriodeTilIntervall(dto.getOpplysningsperiodeSkattegrunnlag()).ifPresent(kobling::setOpplysningsperiodeSkattegrunnlag);
        mapPeriodeTilIntervall(dto.getOpptjeningsperiode()).ifPresent(kobling::setOpptjeningsperiode);

        // Diff & log endringer
        koblingTjeneste.lagre(kobling);
        koblingLås.ifPresent(lås -> koblingTjeneste.oppdaterLåsVersjon(lås));

        return kobling;
    }

    private Optional<IntervallEntitet> mapPeriodeTilIntervall(Periode periode) {
        return Optional.ofNullable(periode == null ? null : IntervallEntitet.fraOgMedTilOgMed(periode.getFom(), periode.getTom()));
    }

    public String triggAsyncInnhent(InnhentRegisterdataRequest dto) {
        Kobling kobling = oppdaterKobling(dto);

        ProsessTaskGruppe taskGruppe = new ProsessTaskGruppe();
        var innhentingTask = ProsessTaskData.forProsessTask(RegisterdataInnhentingTask.class);
        var callbackTask = ProsessTaskData.forProsessTask(CallbackTask.class);
        innhentingTask.setSaksnummer(kobling.getSaksnummer().getVerdi());
        innhentingTask.setProperty(TaskConstants.KOBLING_ID, kobling.getId().toString());
        try {
            innhentingTask.setPayload(JsonObjectMapper.getMapper().writeValueAsString(dto));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Feil i serialisering av innhentingrequest", e);
        }
        innhentingTask.setSaksnummer(kobling.getSaksnummer().getVerdi());
        callbackTask.setProperty(TaskConstants.KOBLING_ID, kobling.getId().toString());

        Optional<GrunnlagReferanse> eksisterendeGrunnlagRef = hentSisteReferanseFor(kobling.getKoblingReferanse());
        eksisterendeGrunnlagRef.map(GrunnlagReferanse::getReferanse)
            .ifPresent(ref -> callbackTask.setProperty(EKSISTERENDE_GRUNNLAG_REF, ref.toString()));

        taskGruppe.addNesteSekvensiell(innhentingTask);
        taskGruppe.addNesteSekvensiell(callbackTask);

        return taskTjeneste.lagre(taskGruppe);
    }

    public Optional<GrunnlagReferanse> hentSisteReferanseFor(KoblingReferanse koblingRef) {
        Optional<Kobling> kobling = koblingTjeneste.hentFor(koblingRef);
        if (kobling.isEmpty()) {
            return Optional.empty();
        }
        Optional<InntektArbeidYtelseGrunnlag> grunnlag = iayTjeneste.hentGrunnlagFor(kobling.get().getKoblingReferanse());
        return grunnlag.map(InntektArbeidYtelseGrunnlag::getGrunnlagReferanse);
    }

}
