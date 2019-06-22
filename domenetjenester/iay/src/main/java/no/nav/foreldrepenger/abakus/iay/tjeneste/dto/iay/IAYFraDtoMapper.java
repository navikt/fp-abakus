package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.Optional;
import java.util.UUID;

import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.v1.InntektArbeidYtelseGrunnlagDto;

public class IAYFraDtoMapper {

    private InntektArbeidYtelseTjeneste iayTjeneste;
    private AktørId aktørId;
    private KoblingReferanse koblingReferanse;

    public IAYFraDtoMapper(InntektArbeidYtelseTjeneste tjeneste, AktørId aktørId, KoblingReferanse koblingReferanse) {
        this.iayTjeneste = tjeneste;
        this.aktørId = aktørId;
        this.koblingReferanse = koblingReferanse;
    }

    /**
     * Mapper oppdaterte data til grunnlag. Kan oppdatere tidligere grunnlag eller siste grunnlag på en kobling. Merk at det ikke nødvendigvis
     * er tillatt å oppdatere tidliger grunnlag alltid (eks. OppgittOpptjening kan kun settes en gang).
     */
    public InntektArbeidYtelseGrunnlag mapTilGrunnlag(InntektArbeidYtelseGrunnlagDto dto) {
        var kladd = hentGrunnlag(dto);
        var builder = !kladd.isPresent()
            ? InntektArbeidYtelseGrunnlagBuilder.ny(UUID.fromString(dto.getGrunnlagReferanse()), dto.getGrunnlagTidspunkt().toLocalDateTime())
            : InntektArbeidYtelseGrunnlagBuilder.oppdatere(kladd.get());

        return mapTilGrunnlag(dto, builder);
    }

    /**
     * @see #mapTilGrunnlag(InntektArbeidYtelseGrunnlagDto)
     */
    public InntektArbeidYtelseGrunnlag mapTilGrunnlag(InntektArbeidYtelseGrunnlagDto dto, InntektArbeidYtelseGrunnlagBuilder builder) {
        mapSaksbehandlerDataTilBuilder(dto, builder);
        mapTilGrunnlagBuilder(dto, builder);
        return builder.build();
    }

    /**
     * Til bruk for migrering (sender inn registerdata, istdf. å hente fra registerne.). Dersom finnes fra før vil denne kaste exception.
     */
    public InntektArbeidYtelseGrunnlag mapTilGrunnlagInklusivRegisterdata(InntektArbeidYtelseGrunnlagDto dto) {

        var kladd = hentGrunnlag(dto);
        if (kladd.isPresent()) {
            throw new IllegalStateException("Kan ikke oppdatere grunnlag med registerdata:" + dto.getGrunnlagReferanse());
        }

        var builder = InntektArbeidYtelseGrunnlagBuilder.ny(UUID.fromString(dto.getGrunnlagReferanse()), dto.getGrunnlagTidspunkt().toLocalDateTime());

        return mapTilGrunnlagInklusivRegisterdata(dto, builder);
    }

    /**
     * @see #mapTilGrunnlagInklusivRegisterdata(InntektArbeidYtelseGrunnlagDto)
     */
    public InntektArbeidYtelseGrunnlag mapTilGrunnlagInklusivRegisterdata(InntektArbeidYtelseGrunnlagDto dto, InntektArbeidYtelseGrunnlagBuilder builder) {
        mapSaksbehandlerDataTilBuilder(dto, builder);
        mapTilGrunnlagBuilder(dto, builder);

        // ta med registerdata til grunnlaget
        mapRegisterDataTilMigrering(dto, builder);

        return builder.build();
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
        if(register==null) return;
        
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

    private void mapSaksbehandlerDataTilBuilder(InntektArbeidYtelseGrunnlagDto dto, InntektArbeidYtelseGrunnlagBuilder builder) {
        var overstyrt = dto.getOverstyrt();
        if (overstyrt != null) {
            var tidspunkt = overstyrt.getOpprettetTidspunkt().toLocalDateTime();
            var saksbehandlerOverstyringer = iayTjeneste.opprettBuilderForSaksbehandlet(koblingReferanse, overstyrt.getEksternReferanse(), tidspunkt);
            var overstyrtAktørArbeid = new MapAktørArbeid.MapFraDto(aktørId, saksbehandlerOverstyringer).map(overstyrt.getArbeid());
            overstyrtAktørArbeid.forEach(saksbehandlerOverstyringer::leggTilAktørArbeid);
            builder.medData(saksbehandlerOverstyringer);
        }
    }

    private void mapTilGrunnlagBuilder(InntektArbeidYtelseGrunnlagDto dto, InntektArbeidYtelseGrunnlagBuilder builder) {

        var arbeidsforholdInformasjonBuilder = new MapArbeidsforholdInformasjon.MapFraDto(builder).map(dto.getArbeidsforholdInformasjon());
        var mapInntektsmeldinger = new MapInntektsmeldinger.MapFraDto();
        var inntektsmeldinger = mapInntektsmeldinger.map(arbeidsforholdInformasjonBuilder, dto.getInntektsmeldinger());
        var oppgittOpptjening = new MapOppgittOpptjening(iayTjeneste).mapFraDto(new KoblingReferanse(dto.getKoblingReferanse()), dto.getOppgittOpptjening());
        var arbeidsforholdInformasjon = arbeidsforholdInformasjonBuilder.build();

        builder.medOppgittOpptjening(oppgittOpptjening);
        builder.setInntektsmeldinger(inntektsmeldinger);
        builder.medInformasjon(arbeidsforholdInformasjon);
    }

}
