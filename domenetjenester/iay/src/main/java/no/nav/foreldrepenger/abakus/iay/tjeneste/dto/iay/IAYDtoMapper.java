package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.MapAktørInntekt.MapTilDto;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.AktørIdPersonident;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.request.InntektArbeidYtelseGrunnlagRequest;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.request.InntektArbeidYtelseGrunnlagRequest.Dataset;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.v1.InntektArbeidYtelseAggregatOverstyrtDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.v1.InntektArbeidYtelseAggregatRegisterDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.v1.InntektArbeidYtelseGrunnlagDto;

public class IAYDtoMapper {

    private InntektArbeidYtelseTjeneste iayTjeneste;
    private AktørId aktørId;
    private GrunnlagReferanse grunnlagReferanse;
    private KoblingReferanse koblingReferanse;

    public IAYDtoMapper(InntektArbeidYtelseTjeneste tjeneste, AktørId aktørId, GrunnlagReferanse grunnlagReferanse, KoblingReferanse koblingReferanse) {
        this.iayTjeneste = tjeneste;
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
            grunnlagTidspunkt, grunnlagReferanse.getReferanse(), koblingReferanse.getReferanse());

        var aggregatOpt = grunnlag.getOpplysningerEtterSkjæringstidspunkt(null);
        if (aggregatOpt.isEmpty()) {
            throw new IllegalStateException("Fant ikke grunnlag: " + grunnlagReferanse);
        }

        // Selektiv mapping avhengig av hva som er forspurt av data

        var arbeidsforholdInfo = grunnlag.getArbeidsforholdInformasjon().orElseThrow();
        
        if (dataset.contains(Dataset.OVERSTYRT)) {
            grunnlag.getArbeidsforholdInformasjon().ifPresent(ai -> {
                var arbeidsforholdInformasjon = new MapArbeidsforholdInformasjon.MapTilDto().map(ai);
                dto.medArbeidsforholdInformasjon(arbeidsforholdInformasjon);
            });
            grunnlag.getSaksbehandletVersjon().ifPresent(a -> mapSaksbehandlerOverstyrteOpplysninger(a, arbeidsforholdInfo, dto));
        }
        
        if (dataset.contains(Dataset.REGISTER)) {
            mapRegisterOpplysninger(aggregatOpt.get(), arbeidsforholdInfo, dto);
        }


        if (dataset.contains(Dataset.INNTEKTSMELDING)) {
            var mapper = new MapInntektsmeldinger.MapTilDto(arbeidsforholdInfo);
            var inntektsmeldinger = mapper.map(grunnlag.getInntektsmeldinger().orElse(null));
            dto.medInntektsmeldinger(inntektsmeldinger);
        }

        if (dataset.contains(Dataset.OPPGITT_OPPTJENING)) {
            grunnlag.getOppgittOpptjening().ifPresent(oo -> {
                var mapper = new MapOppgittOpptjening().mapTilDto(oo);
                dto.medOppgittOpptjening(mapper);
            });
        }

        return dto;
    }

    /** Mapper oppdaterte data til grunnlag. Kan oppdatere tidligere grunnlag eller siste grunnlag på en kobling. */
    public InntektArbeidYtelseGrunnlag mapTilGrunnlag(InntektArbeidYtelseGrunnlagDto dto) {
        var builder = mapGrunnlag(dto);
        return builder.build();
    }

    /**
     * Til bruk for migrering (sender inn registerdata, istdf. å hente fra registerne.)
     */
    public InntektArbeidYtelseGrunnlag mapTilGrunnlagInklusivRegisterdata(InntektArbeidYtelseGrunnlagDto dto) {
        var builder = mapGrunnlag(dto);

        // ta med registerdata til grunnlaget
        mapRegisterDataTilMigrering(dto, builder);

        return builder.build();
    }

    private InntektArbeidYtelseGrunnlagBuilder mapGrunnlag(InntektArbeidYtelseGrunnlagDto dto) {
        var kladd = hentGrunnlag(dto);
        var builder = kladd.isEmpty()
            ? InntektArbeidYtelseGrunnlagBuilder.ny(UUID.fromString(dto.getGrunnlagReferanse()))
            : InntektArbeidYtelseGrunnlagBuilder.oppdatere(kladd.get());

        var overstyringer = mapSaksbehandlerDataTilBuilder(dto, builder);
        builder.medData(overstyringer);

        mapTilGrunnlagBuilder(dto, builder);
        return builder;
    }

    private Optional<InntektArbeidYtelseGrunnlag> hentGrunnlag(InntektArbeidYtelseGrunnlagDto dto) {
        if (dto.getGrunnlagReferanse() == null) {
            if (dto.getKoblingReferanse() != null) {
                return iayTjeneste.hentGrunnlagFor(new KoblingReferanse(dto.getKoblingReferanse()));
            } else {
                return Optional.empty();
            }
        }
        return iayTjeneste.hentGrunnlagFor(new GrunnlagReferanse(dto.getGrunnlagReferanse()));
    }

    // brukes kun til migrering av data (dytter inn IAYG)
    private void mapRegisterDataTilMigrering(InntektArbeidYtelseGrunnlagDto dto, InntektArbeidYtelseGrunnlagBuilder builder) {
        var register = dto.getRegister();
        var tidspunkt = register.getOpprettetTidspunkt().toLocalDateTime();

        var registerBuilder = iayTjeneste.opprettBuilderForRegister(koblingReferanse, register.getEksternReferanse(), tidspunkt);

        var aktørArbeid = new MapAktørArbeid.MapFraDto(aktørId, registerBuilder).map(dto.getRegister().getArbeid());
        var aktørInntekt = new MapAktørInntekt.MapFraDto(aktørId, registerBuilder).map(dto.getRegister().getInntekt());
        var aktørYtelse = new MapAktørYtelse.MapFraDto(aktørId, registerBuilder).map(dto.getRegister().getYtelse());

        aktørArbeid.forEach(registerBuilder::leggTilAktørArbeid);
        aktørInntekt.forEach(registerBuilder::leggTilAktørInntekt);
        aktørYtelse.forEach(registerBuilder::leggTilAktørYtelse);

        builder.medData(registerBuilder);
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

    private InntektArbeidYtelseAggregatBuilder mapSaksbehandlerDataTilBuilder(InntektArbeidYtelseGrunnlagDto dto, InntektArbeidYtelseGrunnlagBuilder builder) {
        var overstyrt = dto.getOverstyrt();
        var tidspunkt = overstyrt.getOpprettetTidspunkt().toLocalDateTime();
        var saksbehandlerOverstyringer = iayTjeneste.opprettBuilderForSaksbehandlet(koblingReferanse, overstyrt.getEksternReferanse(), tidspunkt);
        var overstyrtAktørArbeid = new MapAktørArbeid.MapFraDto(aktørId, saksbehandlerOverstyringer).map(overstyrt.getArbeid());
        overstyrtAktørArbeid.forEach(saksbehandlerOverstyringer::leggTilAktørArbeid);
        return saksbehandlerOverstyringer;
    }

    private void mapTilGrunnlagBuilder(InntektArbeidYtelseGrunnlagDto dto, InntektArbeidYtelseGrunnlagBuilder builder) {

        var arbeidsforholdInformasjonBuilder = new MapArbeidsforholdInformasjon.MapFraDto(builder).map(dto.getArbeidsforholdInformasjon());
        var mapInntektsmeldinger = new MapInntektsmeldinger.MapFraDto();
        var inntektsmeldinger = mapInntektsmeldinger.map(arbeidsforholdInformasjonBuilder, dto.getInntektsmeldinger());
        var oppgittOpptjening = new MapOppgittOpptjening().mapFraDto(dto.getOppgittOpptjening());
        var arbeidsforholdInformasjon = arbeidsforholdInformasjonBuilder.build();

        builder.medOppgittOpptjening(oppgittOpptjening);
        builder.setInntektsmeldinger(inntektsmeldinger);
        builder.medInformasjon(arbeidsforholdInformasjon);
    }

}
