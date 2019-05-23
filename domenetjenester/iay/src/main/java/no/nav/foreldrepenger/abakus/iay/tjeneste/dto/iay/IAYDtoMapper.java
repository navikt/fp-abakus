package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.NotFoundException;

import no.nav.foreldrepenger.abakus.domene.iay.AktørArbeid;
import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektsmeldingAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.InntektsmeldingSomIkkeKommer;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
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
        var grunnlagReferanse2 = grunnlag.getGrunnlagReferanse().getReferanse();
        var koblingReferanse2 = tjeneste.hentKoblingReferanse(new GrunnlagReferanse(grunnlagReferanse2)).getReferanse();

        var tidspunkt = grunnlag.getOpprettetTidspunkt().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        var dto = new InntektArbeidYtelseGrunnlagDto(new AktørIdPersonident(aktørId.getId()), tidspunkt, grunnlagReferanse2, koblingReferanse2);

        var aggregatOpt = grunnlag.getOpplysningerEtterSkjæringstidspunkt(null);
        if (aggregatOpt.isEmpty()) {
            throw new NotFoundException("Fant ikke grunnlag: " + grunnlagReferanse);
        }

        if (dataset.contains(Dataset.REGISTER)) {
            mapRegisterOpplysninger(aggregatOpt, dto);
        }

        if (dataset.contains(Dataset.OVERSTYRT)) {
            grunnlag.getSaksbehandletVersjon().ifPresent(a -> mapSaksbehandlerOverstyrteOpplysninger(a, dto));
        }

        if (dataset.contains(Dataset.INNTEKTSMELDING)) {
            mapInntektsmeldinger(grunnlag.getInntektsmeldinger(), grunnlag.getInntektsmeldingerSomIkkeKommer(), dto);
        }

        if (dataset.contains(Dataset.OPPGITT_OPPTJENING)) {
            mapOpptjening(grunnlag.getOppgittOpptjening(), dto);
        }

        return dto;
    }

    public InntektArbeidYtelseGrunnlag mapTilGrunnlag(InntektArbeidYtelseGrunnlagDto dto) {
        var saksbehandlerOverstyringer = tjeneste.opprettBuilderForSaksbehandlerOverstyring(koblingReferanse);
        var overstyrtAktørArbeid = new MapAktørArbeid(tjeneste, koblingReferanse).mapFraDto(aktørId, saksbehandlerOverstyringer,
            dto.getOverstyrt().getArbeid());
        overstyrtAktørArbeid.forEach(saksbehandlerOverstyringer::leggTilAktørArbeid);

        var inntektsmeldinger = new MapInntektsmeldinger(tjeneste, koblingReferanse).mapFraDto(dto.getInntektsmeldinger());
        var oppgittOpptjening = new MapOppgittOpptjening().mapFraDto(dto.getOppgittOpptjening());

        // Opprett nytt grunnlag (kliss nytt eller basert på annet)
        var kladd = hentGrunnlag(dto);
        var builder = InntektArbeidYtelseGrunnlagBuilder.oppdatere(kladd);
        builder.medOppgittOpptjening(oppgittOpptjening);
        builder.setInntektsmeldinger(inntektsmeldinger);
        builder.medData(saksbehandlerOverstyringer);

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

    public InntektArbeidYtelseGrunnlag mapTilGrunnlagForMigrering(InntektArbeidYtelseGrunnlagDto dto) {
        var saksbehandlerOverstyringer = tjeneste.opprettBuilderForSaksbehandlerOverstyring(koblingReferanse);
        var overstyrtAktørArbeid = new MapAktørArbeid(tjeneste, koblingReferanse).mapFraDto(aktørId, saksbehandlerOverstyringer,
            dto.getOverstyrt().getArbeid());
        overstyrtAktørArbeid.forEach(saksbehandlerOverstyringer::leggTilAktørArbeid);

        var inntektsmeldinger = new MapInntektsmeldinger(tjeneste, koblingReferanse).mapFraDto(dto.getInntektsmeldinger());
        var oppgittOpptjening = new MapOppgittOpptjening().mapFraDto(dto.getOppgittOpptjening());
        var registerData = mapRegisterDataTilMigrering(dto);

        // Opprett nytt grunnlag (kliss nytt eller basert på tidligere)
        var kladd = hentGrunnlag(dto);
        var builder = kladd.isEmpty()
            ? InntektArbeidYtelseGrunnlagBuilder.ny(UUID.fromString(dto.getGrunnlagReferanse()))
            : InntektArbeidYtelseGrunnlagBuilder.oppdatere(kladd.get());
            
        builder.medOppgittOpptjening(oppgittOpptjening);
        builder.setInntektsmeldinger(inntektsmeldinger);
        builder.medData(saksbehandlerOverstyringer);
        builder.medData(registerData);

        return builder.build();
    }

    // brukes kun til migrering av data (dytter inn IAYG)
    private InntektArbeidYtelseAggregatBuilder mapRegisterDataTilMigrering(InntektArbeidYtelseGrunnlagDto dto) {
        var registerData = tjeneste.opprettBuilderForRegister(koblingReferanse);

        var aktørArbeid = new MapAktørArbeid(tjeneste, koblingReferanse).mapFraDto(aktørId, registerData, dto.getRegister().getArbeid());
        var aktørInntekt = new MapAktørInntekt().mapFraDto(aktørId, registerData, dto.getRegister().getInntekt());
        var aktørYtelse = new MapAktørYtelse().mapFraDto(aktørId, registerData, dto.getRegister().getYtelse());

        aktørArbeid.forEach(registerData::leggTilAktørArbeid);
        aktørInntekt.forEach(registerData::leggTilAktørInntekt);
        aktørYtelse.forEach(registerData::leggTilAktørYtelse);

        return registerData;
    }

    private void mapOpptjening(Optional<OppgittOpptjening> oppgittOpptjening,
                               InntektArbeidYtelseGrunnlagDto dto) {
        oppgittOpptjening.ifPresent(oo -> dto.medOppgittOpptjening(new MapOppgittOpptjening().mapTilDto(oo)));

    }

    private void mapInntektsmeldinger(Optional<InntektsmeldingAggregat> inntektsmeldinger,
                                      List<InntektsmeldingSomIkkeKommer> inntektsmeldingerSomIkkeKommer,
                                      InntektArbeidYtelseGrunnlagDto dto) {
        dto.medInntektsmeldinger(
            new MapInntektsmeldinger(tjeneste, koblingReferanse).mapTilDto(inntektsmeldinger.orElse(null), inntektsmeldingerSomIkkeKommer));
    }

    private void mapSaksbehandlerOverstyrteOpplysninger(InntektArbeidYtelseAggregat aggregat,
                                                        InntektArbeidYtelseGrunnlagDto dto) {
        LocalDateTime tidspunkt = aggregat.getOpprettetTidspunkt();
        Collection<AktørArbeid> aktørArbeid = aggregat.getAktørArbeid();
        dto.medOverstyrt(new InntektArbeidYtelseAggregatOverstyrtDto(tidspunkt, aggregat.getEksternReferanse())
            .medArbeid(new MapAktørArbeid(tjeneste, koblingReferanse).mapTilDto(aktørArbeid)));
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
                .medArbeid(new MapAktørArbeid(tjeneste, koblingReferanse).mapTilDto(aggregat.getAktørArbeid()))
                .medInntekt(new MapAktørInntekt().mapTilDto(aggregat.getAktørInntekt()))
                .medYtelse(new MapAktørYtelse().mapTilDto(aggregat.getAktørYtelse())));
    }

}
