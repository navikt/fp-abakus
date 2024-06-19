package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.time.ZoneId;
import java.util.Set;
import java.util.UUID;
import no.nav.abakus.iaygrunnlag.AktørIdPersonident;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.request.Dataset;
import no.nav.abakus.iaygrunnlag.v1.InntektArbeidYtelseAggregatOverstyrtDto;
import no.nav.abakus.iaygrunnlag.v1.InntektArbeidYtelseAggregatRegisterDto;
import no.nav.abakus.iaygrunnlag.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.MapAktørInntekt.MapTilDto;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.typer.AktørId;

public class IAYTilDtoMapper {

    private AktørId aktørId;
    private GrunnlagReferanse grunnlagReferanse;
    private KoblingReferanse koblingReferanse;

    public IAYTilDtoMapper(AktørId aktørId, GrunnlagReferanse grunnlagReferanse, KoblingReferanse koblingReferanse) {
        this.aktørId = aktørId;
        this.grunnlagReferanse = grunnlagReferanse;
        this.koblingReferanse = koblingReferanse;
    }

    public InntektArbeidYtelseGrunnlagDto mapTilDto(
            InntektArbeidYtelseGrunnlag grunnlag, YtelseType ytelseType, Set<Dataset> dataset) {
        if (grunnlag == null) {
            return null;
        }
        var grunnlagTidspunkt =
                grunnlag.getOpprettetTidspunkt().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        UUID denneGrunnlagRef = grunnlagReferanse != null
                ? grunnlagReferanse.getReferanse()
                : grunnlag.getGrunnlagReferanse().getReferanse();
        var dto = new InntektArbeidYtelseGrunnlagDto(
                new AktørIdPersonident(aktørId.getId()),
                grunnlagTidspunkt,
                denneGrunnlagRef,
                koblingReferanse.getReferanse(),
                ytelseType);

        // Selektiv mapping avhengig av hva som er forspurt av data

        if (dataset.contains(Dataset.REGISTER)) {
            grunnlag.getRegisterVersjon()
                    .ifPresent(a -> mapRegisterOpplysninger(a, getArbeidsforholdInformasjon(grunnlag), dto));
        }

        if (dataset.contains(Dataset.OVERSTYRT)) {
            grunnlag.getArbeidsforholdInformasjon().ifPresent(ai -> {
                var arbeidsforholdInformasjon = mapArbeidsforholdInformasjon(denneGrunnlagRef, ai);
                dto.medArbeidsforholdInformasjon(arbeidsforholdInformasjon);
            });
            grunnlag.getSaksbehandletVersjon()
                    .ifPresent(a ->
                            mapSaksbehandlerOverstyrteOpplysninger(a, getArbeidsforholdInformasjon(grunnlag), dto));
        }

        if (dataset.contains(Dataset.INNTEKTSMELDING)) {
            grunnlag.getInntektsmeldinger().ifPresent(ims -> {
                var mapper = new MapInntektsmeldinger.MapTilDto(getArbeidsforholdInformasjon(grunnlag));
                var inntektsmeldinger = mapper.map(ims);
                dto.medInntektsmeldinger(inntektsmeldinger);
            });
        }

        if (dataset.contains(Dataset.OPPGITT_OPPTJENING)) {
            grunnlag.getOppgittOpptjening().ifPresent(oo -> {
                var mapper = new MapOppgittOpptjening().mapTilDto(oo);
                dto.medOppgittOpptjening(mapper);
            });
        }

        if (dataset.contains(Dataset.OVERSTYRT_OPPGITT_OPPTJENING)) {
            grunnlag.getOverstyrtOppgittOpptjening().ifPresent(oo -> {
                var mapper = new MapOppgittOpptjening().mapTilDto(oo);
                dto.medOverstyrtOppgittOpptjening(mapper);
            });
        }
        return dto;
    }

    public no.nav.abakus.iaygrunnlag.arbeidsforhold.v1.ArbeidsforholdInformasjon mapArbeidsforholdInformasjon(
            UUID grunnlagRef, ArbeidsforholdInformasjon ai) {
        return new MapArbeidsforholdInformasjon.MapTilDto().map(grunnlagRef, ai);
    }

    private ArbeidsforholdInformasjon getArbeidsforholdInformasjon(InntektArbeidYtelseGrunnlag grunnlag) {
        return grunnlag.getArbeidsforholdInformasjon()
                .orElseThrow(
                        () -> new IllegalStateException("Mangler ArbeidsforholdInformasjon i grunnlag (påkrevd her): "
                                + grunnlag.getGrunnlagReferanse()));
    }

    private void mapRegisterOpplysninger(
            InntektArbeidYtelseAggregat aggregat,
            ArbeidsforholdInformasjon arbeidsforholdInfo,
            InntektArbeidYtelseGrunnlagDto dto) {
        var tidspunkt = aggregat.getOpprettetTidspunkt();
        var arbeid = new MapAktørArbeid.MapTilDto(arbeidsforholdInfo).map(aggregat.getAktørArbeid());
        var inntekter = new MapTilDto().map(aggregat.getAktørInntekt());
        var ytelser = new MapAktørYtelse.MapTilDto().map(aggregat.getAktørYtelse());

        dto.medRegister(new InntektArbeidYtelseAggregatRegisterDto(tidspunkt, aggregat.getEksternReferanse())
                .medArbeid(arbeid)
                .medInntekt(inntekter)
                .medYtelse(ytelser));
    }

    private void mapSaksbehandlerOverstyrteOpplysninger(
            InntektArbeidYtelseAggregat aggregat,
            ArbeidsforholdInformasjon arbeidsforholdInfo,
            InntektArbeidYtelseGrunnlagDto dto) {
        var tidspunkt = aggregat.getOpprettetTidspunkt();
        var aktørArbeid = aggregat.getAktørArbeid();
        var arbeid = new MapAktørArbeid.MapTilDto(arbeidsforholdInfo).map(aktørArbeid);
        var overstyrt = new InntektArbeidYtelseAggregatOverstyrtDto(tidspunkt, aggregat.getEksternReferanse());
        overstyrt.medArbeid(arbeid);

        dto.medOverstyrt(overstyrt);
    }
}
