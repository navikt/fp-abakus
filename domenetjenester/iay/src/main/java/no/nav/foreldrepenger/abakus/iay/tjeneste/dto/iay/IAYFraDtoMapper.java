package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import no.nav.abakus.iaygrunnlag.arbeidsforhold.v1.ArbeidsforholdInformasjon;
import no.nav.abakus.iaygrunnlag.v1.InntektArbeidYtelseAggregatOverstyrtDto;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.VersjonType;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.typer.AktørId;

import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;

public class IAYFraDtoMapper {

    private InntektArbeidYtelseTjeneste iayTjeneste;
    private AktørId aktørId;
    private KoblingReferanse koblingReferanse;

    public IAYFraDtoMapper(InntektArbeidYtelseTjeneste tjeneste, AktørId aktørId, KoblingReferanse koblingReferanse) {
        this.iayTjeneste = Objects.requireNonNull(tjeneste, "tjeneste");
        this.aktørId = Objects.requireNonNull(aktørId, "aktørId");
        this.koblingReferanse = Objects.requireNonNull(koblingReferanse, "koblingReferanse");
    }

    /**
     * Mapper oppdaterte data til grunnlag. Kan oppdatere tidligere grunnlag eller siste grunnlag på en kobling. Merk at det ikke nødvendigvis
     * er tillatt å oppdatere tidliger grunnlag alltid (eks. OppgittOpptjening kan kun settes en gang).
     */
    public void mapOverstyringerTilGrunnlagBuilder(InntektArbeidYtelseAggregatOverstyrtDto overstyrt,
                                                   ArbeidsforholdInformasjon arbeidsforholdInformasjon,
                                                   InntektArbeidYtelseGrunnlagBuilder builder) {
        var arbeidsforholdInformasjonBuilder = new MapArbeidsforholdInformasjon.MapFraDto(builder).map(arbeidsforholdInformasjon);
        builder.medInformasjon(arbeidsforholdInformasjonBuilder.build());

        if (overstyrt != null) {
            Optional<InntektArbeidYtelseAggregat> aggregatEntitet = iayTjeneste.hentIAYAggregatFor(koblingReferanse, overstyrt.getEksternReferanse());
            if (aggregatEntitet.isPresent()) {
                var aggregatBuilder = InntektArbeidYtelseAggregatBuilder.pekeTil(aggregatEntitet.get(), VersjonType.SAKSBEHANDLET);
                builder.medSaksbehandlet(aggregatBuilder);
            } else {
                var tidspunkt = overstyrt.getOpprettetTidspunkt().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
                var saksbehandlerOverstyringer = iayTjeneste.opprettBuilderForSaksbehandlet(koblingReferanse, overstyrt.getEksternReferanse(),
                    tidspunkt);
                var overstyrtAktørArbeid = new MapAktørArbeid.MapFraDto(aktørId, saksbehandlerOverstyringer).map(overstyrt.getArbeid());
                overstyrtAktørArbeid.forEach(saksbehandlerOverstyringer::leggTilAktørArbeid);
                builder.medSaksbehandlet(saksbehandlerOverstyringer);
            }
        } else {
            builder.medSaksbehandlet(null);  // fjerner saksbehandlet versjon
        }

    }

}
