package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Arbeidsavtale {
    private LocalDate arbeidsavtaleFom;
    private LocalDate arbeidsavtaleTom;
    private BigDecimal stillingsprosent;
    private BigDecimal beregnetAntallTimerPrUke;
    private BigDecimal avtaltArbeidstimerPerUke;
    private LocalDate sisteLønnsendringsdato;
    private boolean erAnsettelsesPerioden;

    private Arbeidsavtale(LocalDate arbeidsavtaleFom, LocalDate arbeidsavtaleTom, BigDecimal stillingsprosent, BigDecimal beregnetAntallTimerPrUke, BigDecimal avtaltArbeidstimerPerUke, LocalDate sisteLønnsendringsdato, boolean erAnsettelsesPerioden) {
        this.arbeidsavtaleFom = arbeidsavtaleFom;
        this.arbeidsavtaleTom = arbeidsavtaleTom;
        this.stillingsprosent = stillingsprosent;
        this.beregnetAntallTimerPrUke = beregnetAntallTimerPrUke;
        this.avtaltArbeidstimerPerUke = avtaltArbeidstimerPerUke;
        this.sisteLønnsendringsdato = sisteLønnsendringsdato;
        this.erAnsettelsesPerioden = erAnsettelsesPerioden;
    }

    public LocalDate getArbeidsavtaleFom() {
        return arbeidsavtaleFom;
    }

    public LocalDate getArbeidsavtaleTom() {
        return arbeidsavtaleTom;
    }

    public BigDecimal getStillingsprosent() {
        return stillingsprosent;
    }

    public BigDecimal getBeregnetAntallTimerPrUke() {
        return beregnetAntallTimerPrUke;
    }

    public BigDecimal getAvtaltArbeidstimerPerUke() {
        return avtaltArbeidstimerPerUke;
    }

    public LocalDate getSisteLønnsendringsdato() {
       return sisteLønnsendringsdato;
    }

    public boolean getErAnsettelsesPerioden() {
        return erAnsettelsesPerioden;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arbeidsavtale that = (Arbeidsavtale) o;
        return erAnsettelsesPerioden == that.erAnsettelsesPerioden &&
            Objects.equals(arbeidsavtaleFom, that.arbeidsavtaleFom) &&
            Objects.equals(arbeidsavtaleTom, that.arbeidsavtaleTom) &&
            Objects.equals(stillingsprosent, that.stillingsprosent) &&
            Objects.equals(beregnetAntallTimerPrUke, that.beregnetAntallTimerPrUke) &&
            Objects.equals(avtaltArbeidstimerPerUke, that.avtaltArbeidstimerPerUke) &&
            Objects.equals(sisteLønnsendringsdato, that.sisteLønnsendringsdato);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsavtaleFom, arbeidsavtaleTom, stillingsprosent, beregnetAntallTimerPrUke, avtaltArbeidstimerPerUke, sisteLønnsendringsdato, erAnsettelsesPerioden);
    }

    @Override
    public String toString() {
        return "Arbeidsavtale{" +
            "arbeidsavtaleFom=" + arbeidsavtaleFom +
            ", arbeidsavtaleTom=" + arbeidsavtaleTom +
            ", stillingsprosent=" + stillingsprosent +
            ", beregnetAntallTimerPrUke=" + beregnetAntallTimerPrUke +
            ", avtaltArbeidstimerPerUke=" + avtaltArbeidstimerPerUke +
            ", sisteLønnsendringsdato=" + sisteLønnsendringsdato +
            ", erAnsettelsesPerioden=" + erAnsettelsesPerioden +
            '}';
    }

    public String toStringCompact() {
        return "Avtale{" +
            "fom=" + arbeidsavtaleFom +
            ", tom=" + arbeidsavtaleTom +
            ", erAnsperiode=" + erAnsettelsesPerioden +
            '}';
    }

    public static class Builder {
        private boolean erAnsettelsesPerioden = false;
        private LocalDate arbeidsavtaleFom;
        private LocalDate arbeidsavtaleTom;
        private LocalDate sisteLønnsendringsdato;
        private BigDecimal stillingsprosent;
        private BigDecimal beregnetAntallTimerPrUke;
        private BigDecimal avtaltArbeidstimerPerUke;

        public Builder medArbeidsavtaleFom(LocalDate arbeidsavtaleFom) {
            this.arbeidsavtaleFom = arbeidsavtaleFom;
            return this;
        }

        public Builder medArbeidsavtaleTom(LocalDate arbeidsavtaleTom) {
            this.arbeidsavtaleTom = arbeidsavtaleTom;
            return this;
        }

        public Builder medSisteLønnsendringsdato(LocalDate sisteLønnsendringsdato) {
            this.sisteLønnsendringsdato = sisteLønnsendringsdato;
            return this;
        }

        public Builder medStillingsprosent(BigDecimal stillingsprosent) {
            this.stillingsprosent = stillingsprosent;
            return this;
        }

        public Builder medBeregnetAntallTimerPrUke(BigDecimal beregnetAntallTimerPrUke) {
            this.beregnetAntallTimerPrUke = beregnetAntallTimerPrUke;
            return this;
        }

        public Builder medAvtaltArbeidstimerPerUke(BigDecimal avtaltArbeidstimerPerUke) {
            this.avtaltArbeidstimerPerUke = avtaltArbeidstimerPerUke;
            return this;
        }

        public Builder erAnsettelsesPerioden() {
            this.erAnsettelsesPerioden = true;
            return this;
        }

        public Arbeidsavtale build() {
            return new Arbeidsavtale(arbeidsavtaleFom, arbeidsavtaleTom, stillingsprosent, beregnetAntallTimerPrUke, avtaltArbeidstimerPerUke, sisteLønnsendringsdato, erAnsettelsesPerioden);
        }
    }
}
