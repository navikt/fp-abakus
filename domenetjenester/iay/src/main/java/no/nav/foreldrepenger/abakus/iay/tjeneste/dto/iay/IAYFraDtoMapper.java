package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.VersjonType;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.v1.InntektArbeidYtelseGrunnlagDto;

public class IAYFraDtoMapper {

    private InntektArbeidYtelseTjeneste iayTjeneste;
    private KodeverkRepository kodeverkRepository;
    private AktørId aktørId;
    private KoblingReferanse koblingReferanse;

    public IAYFraDtoMapper(InntektArbeidYtelseTjeneste tjeneste, KodeverkRepository kodeverkRepository, AktørId aktørId, KoblingReferanse koblingReferanse) {
        this.iayTjeneste = tjeneste;
        this.kodeverkRepository = kodeverkRepository;
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
            ? InntektArbeidYtelseGrunnlagBuilder.ny(UUID.fromString(dto.getGrunnlagReferanse()), dto.getGrunnlagTidspunkt().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
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
     * @see #mapTilGrunnlag(InntektArbeidYtelseGrunnlagDto)
     */
    public InntektArbeidYtelseGrunnlag mapOverstyringerTilGrunnlag(InntektArbeidYtelseGrunnlagDto dto, InntektArbeidYtelseGrunnlagBuilder builder) {
        mapSaksbehandlerDataTilBuilder(dto, builder);
        mapOverstyringer(dto, builder);
        return builder.build();
    }

    /**
     * Til bruk for migrering (sender inn registerdata, istdf. å hente fra registerne.).
     */
    public InntektArbeidYtelseGrunnlag mapTilGrunnlagInklusivRegisterdata(InntektArbeidYtelseGrunnlagDto dto, boolean erAktivtGrunnlag) {
        var builder = InntektArbeidYtelseGrunnlagBuilder.ny(UUID.fromString(dto.getGrunnlagReferanse()), dto.getGrunnlagTidspunkt().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime());
        builder.medErAktivtGrunnlag(erAktivtGrunnlag);
        return mapTilGrunnlagInklusivRegisterdata(dto, builder);
    }

    /**
     * @see #mapTilGrunnlagInklusivRegisterdata(InntektArbeidYtelseGrunnlagDto)
     */
    public InntektArbeidYtelseGrunnlag mapTilGrunnlagInklusivRegisterdata(InntektArbeidYtelseGrunnlagDto dto, InntektArbeidYtelseGrunnlagBuilder builder) {

        // ta med registerdata til grunnlaget
        mapRegisterDataTilMigrering(dto, builder);

        // ta saksbehandler når vi er sikker på å ha fått med register først
        mapSaksbehandlerDataTilBuilder(dto, builder);

        mapTilGrunnlagBuilder(dto, builder);

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
        if (register == null) return;

        Optional<InntektArbeidYtelseAggregat> aggregatEntitet = iayTjeneste.hentIAYAggregatFor(register.getEksternReferanse());
        if (aggregatEntitet.isPresent()) {
            InntektArbeidYtelseAggregatBuilder aggregatBuilder = InntektArbeidYtelseAggregatBuilder.pekeTil(aggregatEntitet.get(), VersjonType.REGISTER);
            builder.medData(aggregatBuilder);
            return;
        }
        var tidspunkt = register.getOpprettetTidspunkt().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();

        var registerBuilder = InntektArbeidYtelseAggregatBuilder.builderFor(Optional.empty(), register.getEksternReferanse(), tidspunkt, VersjonType.REGISTER);

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
            Optional<InntektArbeidYtelseAggregat> aggregatEntitet = iayTjeneste.hentIAYAggregatFor(overstyrt.getEksternReferanse());
            if (aggregatEntitet.isPresent()) {
                var aggregatBuilder = InntektArbeidYtelseAggregatBuilder.pekeTil(aggregatEntitet.get(), VersjonType.SAKSBEHANDLET);
                builder.medData(aggregatBuilder);
                return;
            }
            var tidspunkt = overstyrt.getOpprettetTidspunkt().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
            var saksbehandlerOverstyringer = iayTjeneste.opprettBuilderForSaksbehandlet(koblingReferanse, overstyrt.getEksternReferanse(), tidspunkt);
            var overstyrtAktørArbeid = new MapAktørArbeid.MapFraDto(aktørId, saksbehandlerOverstyringer).map(overstyrt.getArbeid());
            overstyrtAktørArbeid.forEach(saksbehandlerOverstyringer::leggTilAktørArbeid);
            builder.medData(saksbehandlerOverstyringer);
        }
    }

    private void mapTilGrunnlagBuilder(InntektArbeidYtelseGrunnlagDto dto, InntektArbeidYtelseGrunnlagBuilder builder) {

        var arbeidsforholdInformasjonBuilder = new MapArbeidsforholdInformasjon.MapFraDto(kodeverkRepository, builder).map(dto.getArbeidsforholdInformasjon());
        var mapInntektsmeldinger = new MapInntektsmeldinger.MapFraDto();
        var inntektsmeldinger = mapInntektsmeldinger.map(arbeidsforholdInformasjonBuilder, dto.getInntektsmeldinger());
        var arbeidsforholdInformasjon = arbeidsforholdInformasjonBuilder.build();

        builder.setInntektsmeldinger(inntektsmeldinger);
        builder.medInformasjon(arbeidsforholdInformasjon);

        var oppgittOpptjening = new MapOppgittOpptjening(iayTjeneste, kodeverkRepository).mapFraDto(dto.getOppgittOpptjening());
        builder.medOppgittOpptjening(oppgittOpptjening);
    }

    private void mapOverstyringer(InntektArbeidYtelseGrunnlagDto dto, InntektArbeidYtelseGrunnlagBuilder builder) {

        var arbeidsforholdInformasjonBuilder = new MapArbeidsforholdInformasjon.MapFraDto(kodeverkRepository, builder).map(dto.getArbeidsforholdInformasjon());
        var arbeidsforholdInformasjon = arbeidsforholdInformasjonBuilder.build();

        builder.medInformasjon(arbeidsforholdInformasjon);
    }

}
