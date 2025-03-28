package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.iaygrunnlag.request.Dataset;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;

public class InntektArbeidYtelseGrunnlagBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(InntektArbeidYtelseGrunnlagBuilder.class);

    private InntektArbeidYtelseGrunnlag kladd;

    private InntektArbeidYtelseGrunnlagBuilder(InntektArbeidYtelseGrunnlag kladd) {
        this.kladd = kladd;
    }

    public static InntektArbeidYtelseGrunnlagBuilder nytt() {
        return ny(UUID.randomUUID(), LocalDateTime.now());
    }

    /**
     * Brukes ved migrering.
     */
    public static InntektArbeidYtelseGrunnlagBuilder ny(UUID grunnlagReferanse, LocalDateTime opprettetTidspunkt) {
        return new InntektArbeidYtelseGrunnlagBuilder(new InntektArbeidYtelseGrunnlag(grunnlagReferanse, opprettetTidspunkt));
    }

    public static InntektArbeidYtelseGrunnlagBuilder oppdatere(InntektArbeidYtelseGrunnlag kladd) {
        return new InntektArbeidYtelseGrunnlagBuilder(new InntektArbeidYtelseGrunnlag(kladd));
    }

    public static InntektArbeidYtelseGrunnlagBuilder oppdatere(Optional<InntektArbeidYtelseGrunnlag> kladd) {
        return kladd.map(InntektArbeidYtelseGrunnlagBuilder::oppdatere).orElseGet(InntektArbeidYtelseGrunnlagBuilder::nytt);
    }

    public static InntektArbeidYtelseGrunnlagBuilder kopierDeler(InntektArbeidYtelseGrunnlag original, Set<Dataset> dataset) {
        final var kladd = new InntektArbeidYtelseGrunnlag(original);

        if (skalIkkeKopierMed(dataset, Dataset.OPPGITT_OPPTJENING)) {
            kladd.setOppgittOpptjening(null);
        }
        if (skalIkkeKopierMed(dataset, Dataset.OVERSTYRT_OPPGITT_OPPTJENING)) {
            kladd.setOverstyrtOppgittOpptjening(null);
        }
        if (skalIkkeKopierMed(dataset, Dataset.INNTEKTSMELDING)) {
            kladd.setInntektsmeldinger(null);
        }
        if (skalIkkeKopierMed(dataset, Dataset.REGISTER)) {
            kladd.setRegister(null);
        }
        if (skalIkkeKopierMed(dataset, Dataset.OVERSTYRT)) {
            kladd.getArbeidsforholdInformasjon().ifPresent(it -> {
                final var informasjonBuilder = ArbeidsforholdInformasjonBuilder.oppdatere(it).tilbakestillOverstyringer();
                kladd.setInformasjon(informasjonBuilder.build());
            });
            kladd.setSaksbehandlet(null);
        }
        return new InntektArbeidYtelseGrunnlagBuilder(kladd);
    }

    private static boolean skalIkkeKopierMed(Set<Dataset> dataset, Dataset oppgittOpptjening) {
        return !dataset.contains(oppgittOpptjening);
    }

    protected InntektArbeidYtelseGrunnlag getKladd() {
        return kladd;
    }

    protected InntektsmeldingAggregat getInntektsmeldinger() {
        final Optional<InntektsmeldingAggregat> inntektsmeldinger = kladd.getInntektsmeldinger();
        return inntektsmeldinger.map(InntektsmeldingAggregat::new).orElseGet(InntektsmeldingAggregat::new);
    }

    public void setInntektsmeldinger(InntektsmeldingAggregat inntektsmeldinger) {
        kladd.setInntektsmeldinger(inntektsmeldinger);
    }

    public InntektArbeidYtelseAggregatBuilder getRegisterBuilder() {
        return InntektArbeidYtelseAggregatBuilder.oppdatere(kladd.getRegisterVersjon(), VersjonType.REGISTER);
    }

    public ArbeidsforholdInformasjonBuilder getInformasjonBuilder() {
        return ArbeidsforholdInformasjonBuilder.builder(kladd.getArbeidsforholdInformasjon());
    }

    public ArbeidsforholdInformasjon getInformasjon() {
        var informasjon = kladd.getArbeidsforholdInformasjon();

        var informasjonEntitet = informasjon.map(it -> {
            var entitet = it;
            if (entitet.getId() == null) {
                // ulagret, med preparert, returner her istdf å lage nye hver gang.
                return entitet;
            } else {
                return new ArbeidsforholdInformasjon(it);
            }
        }).orElseGet(() -> new ArbeidsforholdInformasjon());
        kladd.setInformasjon(informasjonEntitet);
        return informasjonEntitet;
    }

    public InntektArbeidYtelseGrunnlagBuilder medInformasjon(ArbeidsforholdInformasjon informasjon) {
        kladd.setInformasjon(informasjon);
        return this;
    }

    public InntektArbeidYtelseGrunnlagBuilder medErAktivtGrunnlag(boolean erAktivtGrunnlag) {
        kladd.setAktivt(erAktivtGrunnlag);
        return this;
    }

    public void medSaksbehandlet(InntektArbeidYtelseAggregatBuilder builder) {
        VersjonType forventet = VersjonType.SAKSBEHANDLET;
        if (builder != null && !Objects.equals(builder.getVersjon(), forventet)) {
            throw new IllegalArgumentException("kan kun angi " + forventet + ", fikk: " + builder.getVersjon());
        }
        kladd.setSaksbehandlet(builder == null ? null : builder.build());
    }

    public void medRegister(InntektArbeidYtelseAggregatBuilder builder) {
        VersjonType forventet = VersjonType.REGISTER;
        if (builder != null && !Objects.equals(builder.getVersjon(), forventet)) {
            throw new IllegalArgumentException("kan kun angi " + forventet + ", fikk: " + builder.getVersjon());
        }
        kladd.setRegister(builder == null ? null : builder.build());
    }

    public InntektArbeidYtelseGrunnlagBuilder medOppgittOpptjening(OppgittOpptjeningBuilder builder) {
        if (builder != null) {
            LOG.info("Erstatter eksisterende oppgitt opptjening");
            if (kladd.getOppgittOpptjening().isPresent()) {
                LOG.info("Overskriver eksisterende oppgitt opptjening");
            }
            kladd.setOppgittOpptjening(builder.build());
        }
        return this;
    }

    public InntektArbeidYtelseGrunnlagBuilder medOverstyrtOppgittOpptjening(OppgittOpptjeningBuilder builder) {
        if (builder != null) {
            kladd.setOverstyrtOppgittOpptjening(builder.build());
        }
        return this;
    }

    public InntektArbeidYtelseGrunnlagBuilder fjernOverstyrtOppgittOpptjening() {
        kladd.setOverstyrtOppgittOpptjening(null);
        return this;
    }

    public InntektArbeidYtelseGrunnlagBuilder medDeaktivert() {
        kladd.setAktivt(false);
        return this;
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

    public Optional<ArbeidsforholdInformasjon> getArbeidsforholdInformasjon() {
        return kladd.getArbeidsforholdInformasjon();
    }

}
