package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.NotFoundException;

import org.jboss.weld.exceptions.UnsupportedOperationException;

import no.nav.foreldrepenger.abakus.domene.iay.AktørArbeid;
import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
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
        var dto = new InntektArbeidYtelseGrunnlagDto(new AktørIdPersonident(aktørId.getId()), grunnlag.getGrunnlagReferanse().getReferanse());

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
        // FIXME mapping til IAYG entiteter
        var overstyrtAktørArbeid = new MapAktørArbeid(tjeneste, koblingReferanse).mapFraDto(dto.getOverstyrt().getArbeid());

        // bør denne sendes på samme grensesnitt? gjør det forsåvidt enkelt
        var inntektsmeldinger = new MapInntektsmeldinger(tjeneste, koblingReferanse).mapFraDto(dto.getInntektsmeldinger());

        // bør denne sendes på samme grensesnitt? gjør det forsåvidet enkelt
        var oppgittOpptjening = new MapOppgittOpptjening().mapFraDto(dto.getOppgittOpptjening());

        // disse er kun for migrering?
        var aktørArbeidRegisterMigrering = new MapAktørArbeid(tjeneste, koblingReferanse).mapFraDto(dto.getOverstyrt().getArbeid());
        var aktørInntektRegisterMigrering = new MapAktørInntekt().mapFraDto(dto.getRegister().getInntekt());
        var aktørYtelseRegisterMigrering = new MapAktørYtelse().mapFraDto(dto.getRegister().getYtelse());

        // FIXME ferdigstill dette
        throw new UnsupportedOperationException("Not Yet Implemented");
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
        dto.medOverstyrt(new InntektArbeidYtelseAggregatOverstyrtDto(tidspunkt)
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
            new InntektArbeidYtelseAggregatRegisterDto(tidspunkt)
                .medArbeid(new MapAktørArbeid(tjeneste, koblingReferanse).mapTilDto(aggregat.getAktørArbeid()))
                .medInntekt(new MapAktørInntekt().mapTilDto(aggregat.getAktørInntekt()))
                .medYtelse(new MapAktørYtelse().mapTilDto(aggregat.getAktørYtelse())));
    }

}
