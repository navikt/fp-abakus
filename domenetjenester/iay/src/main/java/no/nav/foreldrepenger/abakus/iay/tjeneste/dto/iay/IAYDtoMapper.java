package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.NotFoundException;

import no.nav.foreldrepenger.abakus.domene.iay.AktørArbeid;
import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.MapAktørArbeid.MapFraDto;
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

    private InntektArbeidYtelseTjeneste tjeneste;
    private AktørId aktørId;
    private GrunnlagReferanse grunnlagReferanse;
    private KoblingReferanse koblingReferanse;

    public IAYDtoMapper(InntektArbeidYtelseTjeneste tjeneste, AktørId aktørId, GrunnlagReferanse grunnlagReferanse, KoblingReferanse koblingReferanse) {
        this.tjeneste = tjeneste;
        this.aktørId = aktørId;
        this.grunnlagReferanse = grunnlagReferanse;
        this.koblingReferanse = koblingReferanse;
    }

    public InntektArbeidYtelseGrunnlagDto mapTilDto(InntektArbeidYtelseGrunnlag grunnlag, InntektArbeidYtelseGrunnlagRequest spec) {
        if (grunnlag == null) {
            return null;
        }
        var dataset = spec.getDataset();

        var tidspunkt = grunnlag.getOpprettetTidspunkt().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        var dto = new InntektArbeidYtelseGrunnlagDto(new AktørIdPersonident(aktørId.getId()), tidspunkt, grunnlagReferanse.getReferanse(), koblingReferanse.getReferanse());

        var aggregatOpt = grunnlag.getOpplysningerEtterSkjæringstidspunkt(null);
        if (aggregatOpt.isEmpty()) {
            throw new NotFoundException("Fant ikke grunnlag: " + grunnlagReferanse);
        }

        // Selektiv mapping avhengig av hva som er forspurt av data
        
        if (dataset.contains(Dataset.REGISTER)) {
            mapRegisterOpplysninger(aggregatOpt, dto);
        }

        if (dataset.contains(Dataset.OVERSTYRT)) {
            grunnlag.getSaksbehandletVersjon().ifPresent(a -> mapSaksbehandlerOverstyrteOpplysninger(a, dto));
        }

        if (dataset.contains(Dataset.INNTEKTSMELDING)) {
            dto.medInntektsmeldinger(
                new MapInntektsmeldinger(tjeneste, koblingReferanse).mapTilDto(grunnlag.getInntektsmeldinger().orElse(null),
                    grunnlag.getInntektsmeldingerSomIkkeKommer()));
        }

        if (dataset.contains(Dataset.OPPGITT_OPPTJENING)) {
            grunnlag.getOppgittOpptjening().ifPresent(oo -> dto.medOppgittOpptjening(new MapOppgittOpptjening().mapTilDto(oo)));
        }

        return dto;
    }

    /** Mapper oppdaterte data til grunnlag.  Kan oppdatere tidligere grunnlag eller siste grunnlag på en kobling. */
    public InntektArbeidYtelseGrunnlag mapTilGrunnlag(InntektArbeidYtelseGrunnlagDto dto) {
        // Opprett nytt grunnlag (kliss nytt eller basert på annet)
        var kladd = hentGrunnlag(dto);
        var builder = InntektArbeidYtelseGrunnlagBuilder.oppdatere(kladd);
        mapTilGrunnlagBuilder(dto, builder);

        return builder.build();

    }

    /**
     * Til bruk for migrering (sender inn registerdata, istdf. å hente fra registerne.)
     */
    public InntektArbeidYtelseGrunnlag mapTilGrunnlagInklusivRegisterdata(InntektArbeidYtelseGrunnlagDto dto) {
        var kladd = hentGrunnlag(dto);
        var builder = kladd.isEmpty()
            ? InntektArbeidYtelseGrunnlagBuilder.ny(UUID.fromString(dto.getGrunnlagReferanse()))
            : InntektArbeidYtelseGrunnlagBuilder.oppdatere(kladd.get());

        mapTilGrunnlagBuilder(dto, builder);

        // ta med registerdata til grunnlaget
        var registerData = mapRegisterDataTilMigrering(dto);
        builder.medData(registerData);

        return builder.build();
    }

    private Optional<InntektArbeidYtelseGrunnlag> hentGrunnlag(InntektArbeidYtelseGrunnlagDto dto) {
        if (dto.getGrunnlagReferanse() == null) {
            if (dto.getKoblingReferanse() != null) {
                return tjeneste.hentGrunnlagFor(new KoblingReferanse(dto.getKoblingReferanse()));
            } else {
                return Optional.empty();
            }
        }
        return tjeneste.hentGrunnlagFor(new GrunnlagReferanse(dto.getGrunnlagReferanse()));
    }

    // brukes kun til migrering av data (dytter inn IAYG)
    private InntektArbeidYtelseAggregatBuilder mapRegisterDataTilMigrering(InntektArbeidYtelseGrunnlagDto dto) {
        var registerDataBuilder = tjeneste.opprettBuilderForRegister(koblingReferanse);

        var aktørArbeid = new MapAktørArbeid.MapFraDto(aktørId, registerDataBuilder).map(dto.getRegister().getArbeid());
        var aktørInntekt = new MapAktørInntekt.MapFraDto(aktørId, registerDataBuilder).map(dto.getRegister().getInntekt());
        var aktørYtelse = new MapAktørYtelse.MapFraDto(aktørId, registerDataBuilder).map(dto.getRegister().getYtelse());

        aktørArbeid.forEach(registerDataBuilder::leggTilAktørArbeid);
        aktørInntekt.forEach(registerDataBuilder::leggTilAktørInntekt);
        aktørYtelse.forEach(registerDataBuilder::leggTilAktørYtelse);

        return registerDataBuilder;
    }

    private void mapRegisterOpplysninger(Optional<InntektArbeidYtelseAggregat> aggregatOpt,
                                         InntektArbeidYtelseGrunnlagDto dto) {
        if (aggregatOpt.isEmpty()) {
            return;
        }
        InntektArbeidYtelseAggregat aggregat = aggregatOpt.get();
        var tidspunkt = aggregat.getOpprettetTidspunkt();
        dto.medRegister(
            new InntektArbeidYtelseAggregatRegisterDto(tidspunkt, aggregat.getEksternReferanse())
                .medArbeid(new MapAktørArbeid.MapTilDto(tjeneste, koblingReferanse).map(aggregat.getAktørArbeid()))
                .medInntekt(new MapTilDto().map(aggregat.getAktørInntekt()))
                .medYtelse(new MapAktørYtelse.MapTilDto().map(aggregat.getAktørYtelse())));
    }

    private void mapSaksbehandlerOverstyrteOpplysninger(InntektArbeidYtelseAggregat aggregat,
                                                        InntektArbeidYtelseGrunnlagDto dto) {
        LocalDateTime tidspunkt = aggregat.getOpprettetTidspunkt();
        Collection<AktørArbeid> aktørArbeid = aggregat.getAktørArbeid();
        dto.medOverstyrt(new InntektArbeidYtelseAggregatOverstyrtDto(tidspunkt, aggregat.getEksternReferanse())
            .medArbeid(new MapAktørArbeid.MapTilDto(tjeneste, koblingReferanse).map(aktørArbeid)));
    }

    private void mapTilGrunnlagBuilder(InntektArbeidYtelseGrunnlagDto dto, InntektArbeidYtelseGrunnlagBuilder builder) {
        var saksbehandlerOverstyringer = tjeneste.opprettBuilderForSaksbehandlerOverstyring(koblingReferanse);
        var overstyrtAktørArbeid = new MapFraDto(aktørId, saksbehandlerOverstyringer).map(dto.getOverstyrt().getArbeid());
        overstyrtAktørArbeid.forEach(saksbehandlerOverstyringer::leggTilAktørArbeid);

        var inntektsmeldinger = new MapInntektsmeldinger(tjeneste, koblingReferanse).mapFraDto(dto.getInntektsmeldinger());
        var oppgittOpptjening = new MapOppgittOpptjening().mapFraDto(dto.getOppgittOpptjening());

        builder.medOppgittOpptjening(oppgittOpptjening);
        builder.setInntektsmeldinger(inntektsmeldinger);
        builder.medData(saksbehandlerOverstyringer);
    }

}
