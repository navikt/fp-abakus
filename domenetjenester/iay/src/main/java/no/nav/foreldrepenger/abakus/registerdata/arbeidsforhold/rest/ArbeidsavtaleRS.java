package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ArbeidsavtaleRS (@JsonProperty("stillingsprosent") BigDecimal stillingsprosent,
                               @JsonProperty("antallTimerPrUke") BigDecimal antallTimerPrUke,
                               @JsonProperty("beregnetAntallTimerPrUke") BigDecimal beregnetAntallTimerPrUke,
                               @JsonProperty("sistLoennsendring") LocalDate sistLoennsendring,
                               @JsonProperty("gyldighetsperiode") PeriodeRS gyldighetsperiode) {

    @Override
    public String toString() {
        return "ArbeidsavtaleRS{" + "stillingsprosent=" + stillingsprosent + ", antallTimerPrUke=" + antallTimerPrUke + ", beregnetAntallTimerPrUke="
            + beregnetAntallTimerPrUke + ", sistLoennsendring=" + sistLoennsendring + ", gyldighetsperiode=" + gyldighetsperiode + '}';
    }
}
