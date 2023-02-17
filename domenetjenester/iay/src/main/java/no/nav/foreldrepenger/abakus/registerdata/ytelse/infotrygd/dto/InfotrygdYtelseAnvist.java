package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class InfotrygdYtelseAnvist {

    private LocalDate utbetaltFom;
    private LocalDate utbetaltTom;
    private BigDecimal utbetalingsgrad;
    private String orgnr;
    private Boolean erRefusjon;
    private BigDecimal dagsats;

    public InfotrygdYtelseAnvist(LocalDate utbetaltFom,
                                 LocalDate utbetaltTom,
                                 BigDecimal utbetalingsgrad,
                                 String orgnr,
                                 Boolean erRefusjon,
                                 BigDecimal dagsats) {
        this.utbetaltFom = utbetaltFom;
        this.utbetaltTom = utbetaltTom;
        this.utbetalingsgrad = utbetalingsgrad;
        this.orgnr = orgnr;
        this.erRefusjon = erRefusjon;
        this.dagsats = dagsats;
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

    public String getOrgnr() {
        return orgnr;
    }

    public Boolean getErRefusjon() {
        return erRefusjon;
    }

    public BigDecimal getDagsats() {
        return dagsats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InfotrygdYtelseAnvist that = (InfotrygdYtelseAnvist) o;
        return Objects.equals(utbetaltFom, that.utbetaltFom) && Objects.equals(utbetaltTom, that.utbetaltTom) && Objects.equals(utbetalingsgrad,
            that.utbetalingsgrad) && Objects.equals(orgnr, that.orgnr) && Objects.equals(erRefusjon, that.erRefusjon) && Objects.equals(dagsats,
            that.dagsats);
    }

    @Override
    public int hashCode() {
        return Objects.hash(utbetaltFom, utbetaltTom, utbetalingsgrad, orgnr, erRefusjon, dagsats);
    }

    @Override
    public String toString() {
        return "InfotrygdYtelseAnvist{" + "utbetaltFom=" + utbetaltFom + ", utbetaltTom=" + utbetaltTom + ", utbetalingsgrad=" + utbetalingsgrad
            + ", orgnr=" + getOrgnrString() + ", erRefusjon=" + erRefusjon + ", dagsats=" + dagsats +

            '}';
    }

    private String getOrgnrString() {
        if (orgnr == null) {
            return null;
        }
        int length = orgnr.length();
        if (length <= 4) {
            return "*".repeat(length);
        }
        return "*".repeat(length - 4) + orgnr.substring(length - 4);
    }

}
