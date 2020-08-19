package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.time.ZoneId;
import java.util.Optional;

import no.nav.abakus.iaygrunnlag.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.VersjonType;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.typer.AktørId;

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
    public InntektArbeidYtelseGrunnlag mapOverstyringerTilGrunnlag(InntektArbeidYtelseGrunnlagDto dto, InntektArbeidYtelseGrunnlagBuilder builder) {
        mapSaksbehandlerDataTilBuilder(dto, builder);
        mapOverstyringer(dto, builder);
        return builder.build();
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

    private void mapOverstyringer(InntektArbeidYtelseGrunnlagDto dto, InntektArbeidYtelseGrunnlagBuilder builder) {

        var arbeidsforholdInformasjonBuilder = new MapArbeidsforholdInformasjon.MapFraDto(builder).map(dto.getArbeidsforholdInformasjon());
        var arbeidsforholdInformasjon = arbeidsforholdInformasjonBuilder.build();

        builder.medInformasjon(arbeidsforholdInformasjon);
    }

}
