package no.nav.foreldrepenger.abakus.registerdata.ytelse.arena;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class MeldekortUtbetalingsgrunnlagMeldekort {

    private LocalDate meldekortFom;
    private LocalDate meldekortTom;
    private BigDecimal dagsats;
    private BigDecimal beløp;
    private BigDecimal utbetalingsgrad;

    private MeldekortUtbetalingsgrunnlagMeldekort() {
    }

    public LocalDate getMeldekortFom() {
        return meldekortFom;
    }

    public LocalDate getMeldekortTom() {
        return meldekortTom;
    }

    public BigDecimal getDagsats() {
        return dagsats;
    }

    public BigDecimal getBeløp() {
        return beløp;
    }

    public BigDecimal getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MeldekortUtbetalingsgrunnlagMeldekort that = (MeldekortUtbetalingsgrunnlagMeldekort) o;
        return Objects.equals(meldekortFom, that.meldekortFom) && Objects.equals(meldekortTom, that.meldekortTom) && Objects.equals(dagsats,
            that.dagsats) && Objects.equals(beløp, that.beløp) && Objects.equals(utbetalingsgrad, that.utbetalingsgrad);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meldekortFom, meldekortTom, dagsats, beløp, utbetalingsgrad);
    }

    @Override
    public String toString() {
        return "MeldekortUtbetalingsgrunnlagMeldekort{" + "meldekortFom=" + meldekortFom + ", meldekortTom=" + meldekortTom + ", dagsats=" + dagsats
            + ", beløp=" + beløp + ", utbetalingsgrad=" + utbetalingsgrad + '}';
    }

    public static class MeldekortMeldekortBuilder {
        private final MeldekortUtbetalingsgrunnlagMeldekort meldekort;

        MeldekortMeldekortBuilder(MeldekortUtbetalingsgrunnlagMeldekort meldekort) {
            this.meldekort = meldekort;
        }

        public static MeldekortMeldekortBuilder ny() {
            return new MeldekortMeldekortBuilder(new MeldekortUtbetalingsgrunnlagMeldekort());
        }

        public MeldekortMeldekortBuilder medMeldekortFom(LocalDate dato) {
            this.meldekort.meldekortFom = dato;
            return this;
        }

        public MeldekortMeldekortBuilder medMeldekortTom(LocalDate dato) {
            this.meldekort.meldekortTom = dato;
            return this;
        }

        public MeldekortMeldekortBuilder medDagsats(BigDecimal dagsats) {
            this.meldekort.dagsats = dagsats;
            return this;
        }

        public MeldekortMeldekortBuilder medBeløp(BigDecimal beløp) {
            this.meldekort.beløp = beløp;
            return this;
        }

        public MeldekortMeldekortBuilder medUtbetalingsgrad(BigDecimal utbetalingsgrad) {
            this.meldekort.utbetalingsgrad = utbetalingsgrad;
            return this;
        }

        public MeldekortUtbetalingsgrunnlagMeldekort build() {
            return this.meldekort;
        }

    }
}
