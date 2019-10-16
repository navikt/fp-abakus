package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.time.ZoneId;

import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.MapAktørInntekt.MapTilDto;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.AktørIdPersonident;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.request.InntektArbeidYtelseGrunnlagRequest;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.request.InntektArbeidYtelseGrunnlagRequest.Dataset;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.v1.InntektArbeidYtelseAggregatOverstyrtDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.v1.InntektArbeidYtelseAggregatRegisterDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.v1.InntektArbeidYtelseGrunnlagDto;

public class IAYTilDtoMapper {

    private AktørId aktørId;
    private GrunnlagReferanse grunnlagReferanse;
    private KoblingReferanse koblingReferanse;

    public IAYTilDtoMapper(AktørId aktørId, GrunnlagReferanse grunnlagReferanse, KoblingReferanse koblingReferanse) {
        this.aktørId = aktørId;
        this.grunnlagReferanse = grunnlagReferanse;
        this.koblingReferanse = koblingReferanse;
    }

    public InntektArbeidYtelseGrunnlagDto mapTilDto(InntektArbeidYtelseGrunnlag grunnlag, InntektArbeidYtelseGrunnlagRequest spec) {
        if (grunnlag == null) {
            return null;
        }
        var dataset = spec.getDataset();

        var grunnlagTidspunkt = grunnlag.getOpprettetTidspunkt().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        var dto = new InntektArbeidYtelseGrunnlagDto(new AktørIdPersonident(aktørId.getId()),
            grunnlagTidspunkt, grunnlagReferanse != null ? grunnlagReferanse.getReferanse() : grunnlag.getGrunnlagReferanse().getReferanse(), koblingReferanse.getReferanse());

        // Selektiv mapping avhengig av hva som er forspurt av data

        if (dataset.contains(Dataset.REGISTER)) {
            grunnlag.getRegisterVersjon().ifPresent(a -> mapRegisterOpplysninger(a, getArbeidsforholdInformasjon(grunnlag), dto));
        }

        if (dataset.contains(Dataset.OVERSTYRT)) {
            grunnlag.getArbeidsforholdInformasjon().ifPresent(ai -> {
                var arbeidsforholdInformasjon = new MapArbeidsforholdInformasjon.MapTilDto().map(ai);
                dto.medArbeidsforholdInformasjon(arbeidsforholdInformasjon);
            });
            grunnlag.getSaksbehandletVersjon().ifPresent(a -> mapSaksbehandlerOverstyrteOpplysninger(a, getArbeidsforholdInformasjon(grunnlag), dto));
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
                var mapper = new MapOppgittOpptjening(null, null).mapTilDto(oo);
                dto.medOppgittOpptjening(mapper);
            });
        }

        return dto;
    }

    private ArbeidsforholdInformasjon getArbeidsforholdInformasjon(InntektArbeidYtelseGrunnlag grunnlag) {
        return grunnlag.getArbeidsforholdInformasjon()
            .orElseThrow(() -> new IllegalStateException("Mangler ArbeidsforholdInformasjon i grunnlag (påkrevd her): " + grunnlag.getGrunnlagReferanse()));
    }

    private void mapRegisterOpplysninger(InntektArbeidYtelseAggregat aggregat,
                                         ArbeidsforholdInformasjon arbeidsforholdInfo,
                                         InntektArbeidYtelseGrunnlagDto dto) {
        var tidspunkt = aggregat.getOpprettetTidspunkt();
        var arbeid = new MapAktørArbeid.MapTilDto(arbeidsforholdInfo).map(aggregat.getAktørArbeid());
        var inntekter = new MapTilDto().map(aggregat.getAktørInntekt());
        var ytelser = new MapAktørYtelse.MapTilDto().map(aggregat.getAktørYtelse());

        dto.medRegister(
            new InntektArbeidYtelseAggregatRegisterDto(tidspunkt, aggregat.getEksternReferanse())
                .medArbeid(arbeid)
                .medInntekt(inntekter)
                .medYtelse(ytelser));
    }

    private void mapSaksbehandlerOverstyrteOpplysninger(InntektArbeidYtelseAggregat aggregat,
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
