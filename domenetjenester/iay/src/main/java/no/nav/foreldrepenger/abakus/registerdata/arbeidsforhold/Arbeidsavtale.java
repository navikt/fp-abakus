package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Arbeidsavtale {
    private LocalDate arbeidsavtaleFom;
    private LocalDate arbeidsavtaleTom;
    private BigDecimal stillingsprosent;
    private LocalDate sisteLønnsendringsdato;
    private boolean erAnsettelsesPerioden;

    private Arbeidsavtale(LocalDate arbeidsavtaleFom,
                          LocalDate arbeidsavtaleTom,
                          BigDecimal stillingsprosent,
                          LocalDate sisteLønnsendringsdato,
                          boolean erAnsettelsesPerioden) {
        this.arbeidsavtaleFom = arbeidsavtaleFom;
        this.arbeidsavtaleTom = arbeidsavtaleTom;
        this.stillingsprosent = stillingsprosent;
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

    public LocalDate getSisteLønnsendringsdato() {
        return sisteLønnsendringsdato;
    }

    public boolean getErAnsettelsesPerioden() {
        return erAnsettelsesPerioden;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Arbeidsavtale that = (Arbeidsavtale) o;
        return erAnsettelsesPerioden == that.erAnsettelsesPerioden && Objects.equals(arbeidsavtaleFom, that.arbeidsavtaleFom) && Objects.equals(
            arbeidsavtaleTom, that.arbeidsavtaleTom) && Objects.equals(stillingsprosent, that.stillingsprosent) && Objects.equals(
            sisteLønnsendringsdato, that.sisteLønnsendringsdato);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsavtaleFom, arbeidsavtaleTom, stillingsprosent, sisteLønnsendringsdato, erAnsettelsesPerioden);
    }

    @Override
    public String toString() {
        return "Arbeidsavtale{" + "arbeidsavtaleFom=" + arbeidsavtaleFom + ", arbeidsavtaleTom=" + arbeidsavtaleTom + ", stillingsprosent="
            + stillingsprosent + ", sisteLønnsendringsdato=" + sisteLønnsendringsdato + ", erAnsettelsesPerioden=" + erAnsettelsesPerioden + '}';
    }

    public static class Builder {
        private boolean erAnsettelsesPerioden = false;
        private LocalDate arbeidsavtaleFom;
        private LocalDate arbeidsavtaleTom;
        private LocalDate sisteLønnsendringsdato;
        private BigDecimal stillingsprosent;

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

        public Builder erAnsettelsesPerioden() {
            this.erAnsettelsesPerioden = true;
            return this;
        }

        public Arbeidsavtale build() {
            return new Arbeidsavtale(arbeidsavtaleFom, arbeidsavtaleTom, stillingsprosent, sisteLønnsendringsdato, erAnsettelsesPerioden);
        }
    }
}
