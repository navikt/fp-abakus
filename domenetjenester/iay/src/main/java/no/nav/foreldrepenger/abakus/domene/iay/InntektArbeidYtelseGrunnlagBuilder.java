package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningEntitet;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.vedtak.util.Tuple;

public class InntektArbeidYtelseGrunnlagBuilder {

    private InntektArbeidYtelseGrunnlag kladd;

    private InntektArbeidYtelseGrunnlagBuilder(InntektArbeidYtelseGrunnlag kladd) {
        this.kladd = kladd;
    }

    public static InntektArbeidYtelseGrunnlagBuilder nytt() {
        return ny(UUID.randomUUID(), LocalDateTime.now());
    }

    /** Brukes ved migrering. */
    public static InntektArbeidYtelseGrunnlagBuilder ny(UUID grunnlagReferanse, LocalDateTime opprettetTidspunkt) {
        return new InntektArbeidYtelseGrunnlagBuilder(new InntektArbeidYtelseGrunnlag(grunnlagReferanse, opprettetTidspunkt));
    }

    public static InntektArbeidYtelseGrunnlagBuilder oppdatere(InntektArbeidYtelseGrunnlag kladd) {
        return new InntektArbeidYtelseGrunnlagBuilder(new InntektArbeidYtelseGrunnlag(kladd));
    }

    public static InntektArbeidYtelseGrunnlagBuilder oppdatere(Optional<InntektArbeidYtelseGrunnlag> kladd) {
        return kladd.map(InntektArbeidYtelseGrunnlagBuilder::oppdatere).orElseGet(InntektArbeidYtelseGrunnlagBuilder::nytt);
    }

    protected InntektArbeidYtelseGrunnlag getKladd() {
        return kladd;
    }

    protected void fjernSaksbehandlet() {
        kladd.fjernSaksbehandlet();
    }

    protected InntektsmeldingAggregat getInntektsmeldinger() {
        final Optional<InntektsmeldingAggregat> inntektsmeldinger = kladd.getInntektsmeldinger();
        return inntektsmeldinger.map(InntektsmeldingAggregat::new).orElseGet(InntektsmeldingAggregat::new);
    }

    public void setInntektsmeldinger(InntektsmeldingAggregat inntektsmeldinger) {
        kladd.setInntektsmeldinger(inntektsmeldinger);
    }

    public ArbeidsforholdInformasjon getInformasjon() {
        var informasjon = kladd.getArbeidsforholdInformasjon();

        var informasjonEntitet = informasjon
            .map(it -> {
                var entitet = (ArbeidsforholdInformasjon) it;
                if (entitet.getId() == null) {
                    // ulagret, med preparert, returner her istdf å lage nye hver gang.
                    return entitet;
                } else {
                    return new ArbeidsforholdInformasjon(it);
                }
            })
            .orElseGet(() -> new ArbeidsforholdInformasjon());
        kladd.setInformasjon(informasjonEntitet);
        return informasjonEntitet;
    }

    public InntektArbeidYtelseGrunnlagBuilder medInformasjon(ArbeidsforholdInformasjon informasjon) {
        kladd.setInformasjon((ArbeidsforholdInformasjon) informasjon);
        return this;
    }

    public InntektArbeidYtelseGrunnlagBuilder medErAktivtGrunnlag(boolean erAktivtGrunnlag) {
        kladd.setAktivt(erAktivtGrunnlag);
        return this;
    }

    private void medSaksbehandlet(InntektArbeidYtelseAggregatBuilder builder) {
        if (builder != null) {
            kladd.setSaksbehandlet((InntektArbeidYtelseAggregatEntitet) builder.build());
        }
    }

    private void medRegister(InntektArbeidYtelseAggregatBuilder builder) {
        if (builder != null) {
            kladd.setRegister((InntektArbeidYtelseAggregatEntitet) builder.build());
        }
    }

    public InntektArbeidYtelseGrunnlagBuilder medOppgittOpptjening(OppgittOpptjeningBuilder builder) {
        if (builder != null) {
            if (kladd.getOppgittOpptjening().isPresent() && erIkkeSammeSomSist(builder)) {
                throw new IllegalStateException("Utviklerfeil: Er ikke lov å endre oppgitt opptjening!");
            }
            kladd.setOppgittOpptjening((OppgittOpptjeningEntitet) builder.build());
        }
        return this;
    }

    private boolean erIkkeSammeSomSist(OppgittOpptjeningBuilder builder) {
        return !kladd.getOppgittOpptjening().get().getEksternReferanse().equals(builder.getEksternReferanse());
    }

    public InntektArbeidYtelseGrunnlag build() {
        var k = kladd;
        kladd = null; // må ikke finne på å gjenbruke buildere her, tar heller straffen i en NPE ved første feilkall
        return k;
    }

    public InntektArbeidYtelseGrunnlagBuilder medData(InntektArbeidYtelseAggregatBuilder builder) {
        VersjonType versjon = builder.getVersjon();

        if (versjon == VersjonType.REGISTER) {
            medRegister(builder);
        } else if (versjon == VersjonType.SAKSBEHANDLET) {
            medSaksbehandlet(builder);
        }
        return this;
    }

    void ryddOppErstattedeArbeidsforhold(AktørId søker, List<Tuple<Arbeidsgiver, Tuple<InternArbeidsforholdRef, InternArbeidsforholdRef>>> erstattArbeidsforhold) {
        final Optional<InntektArbeidYtelseAggregat> registerFørVersjon = kladd.getRegisterVersjon();
        for (Tuple<Arbeidsgiver, Tuple<InternArbeidsforholdRef, InternArbeidsforholdRef>> tuple : erstattArbeidsforhold) {
            if (registerFørVersjon.isPresent()) {
                final InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder.oppdatere(registerFørVersjon, VersjonType.REGISTER);
                builder.oppdaterArbeidsforholdReferanseEtterErstatting(søker, tuple.getElement1(), tuple.getElement2().getElement1(),
                    tuple.getElement2().getElement2());
                medData(builder);
            }
        }
    }

    public Optional<ArbeidsforholdInformasjon> getArbeidsforholdInformasjon() {
        return kladd.getArbeidsforholdInformasjon();
    }

}
