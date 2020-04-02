package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class InfotrygdYtelseAnvist {

    private LocalDate utbetaltFom;
    private LocalDate utbetaltTom;
    private BigDecimal utbetalingsgrad;

    public InfotrygdYtelseAnvist(LocalDate utbetaltFom, LocalDate utbetaltTom, BigDecimal utbetalingsgrad) {
        this.utbetaltFom = utbetaltFom;
        this.utbetaltTom = utbetaltTom;
        this.utbetalingsgrad = utbetalingsgrad;
    }

    public InfotrygdYtelseAnvist(LocalDate utbetaltFom, LocalDate utbetaltTom, int utbetalingsgrad) {
        this.utbetaltFom = utbetaltFom;
        this.utbetaltTom = utbetaltTom;
        this.utbetalingsgrad = new BigDecimal(utbetalingsgrad);
    }

    public LocalDate getUtbetaltFom() {
        return utbetaltFom;
    }

    public LocalDate getUtbetaltTom() {
        return utbetaltTom;
    }

    public BigDecimal getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InfotrygdYtelseAnvist that = (InfotrygdYtelseAnvist) o;
        return Objects.equals(utbetaltFom, that.utbetaltFom) &&
            Objects.equals(utbetaltTom, that.utbetaltTom) &&
            Objects.equals(utbetalingsgrad, that.utbetalingsgrad);
    }

    @Override
    public int hashCode() {
        return Objects.hash(utbetaltFom, utbetaltTom, utbetalingsgrad);
    }

    @Override
    public String toString() {
        return "InfotrygdYtelseAnvist{" +
            "utbetaltFom=" + utbetaltFom +
            ", utbetaltTom=" + utbetaltTom +
            ", utbetalingsgrad=" + utbetalingsgrad +
            '}';
    }
}
