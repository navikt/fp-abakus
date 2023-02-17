package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ArbeidsavtaleRS {

    @JsonProperty("stillingsprosent")
    private BigDecimal stillingsprosent;
    @JsonProperty("antallTimerPrUke")
    private BigDecimal antallTimerPrUke;
    @JsonProperty("beregnetAntallTimerPrUke")
    private BigDecimal beregnetAntallTimerPrUke;
    @JsonProperty("sistLoennsendring")
    private LocalDate sistLoennsendring;
    @JsonProperty("gyldighetsperiode")
    private PeriodeRS gyldighetsperiode;

    public BigDecimal getStillingsprosent() {
        return stillingsprosent;
    }

    public BigDecimal getAntallTimerPrUke() {
        return antallTimerPrUke;
    }

    public BigDecimal getBeregnetAntallTimerPrUke() {
        return beregnetAntallTimerPrUke;
    }

    public LocalDate getSistLoennsendring() {
        return sistLoennsendring;
    }

    public PeriodeRS getGyldighetsperiode() {
        return gyldighetsperiode;
    }

    @Override
    public String toString() {
        return "ArbeidsavtaleRS{" + "stillingsprosent=" + stillingsprosent + ", antallTimerPrUke=" + antallTimerPrUke + ", beregnetAntallTimerPrUke="
            + beregnetAntallTimerPrUke + ", sistLoennsendring=" + sistLoennsendring + ", gyldighetsperiode=" + gyldighetsperiode + '}';
    }
}
