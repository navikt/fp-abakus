package no.nav.abakus.iaygrunnlag.inntekt.v1;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.abakus.iaygrunnlag.kodeverk.SkatteOgAvgiftsregelType;
import no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltYtelseType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class UtbetalingsPostDto {

    @JsonProperty(value = "inntektspostType", required = true)
    @NotNull
    private InntektspostType inntektspostType;

    @JsonProperty(value = "periode", required = true)
    @NotNull
    @Valid
    private Periode periode;

    @JsonProperty("skattAvgiftType")
    private SkatteOgAvgiftsregelType skattAvgiftType;

    /**
     * Tillater her både positive og negative beløp (korreksjoner). Min/max verdi håndteres av mottager og avsender.
     */
    @JsonProperty("beløp")
    @Valid
    @NotNull
    private BigDecimal beløp;

    /**
     * Satt dersom dette gjelder en ytelse, ellers ikke (henger sammen med {@link UtbetalingDto#getKilde()})
     */
    @JsonProperty(value = "ytelseType")
    @Valid
    private WrapUtbetaltYtelse ytelseType;

    protected UtbetalingsPostDto() {
    }

    public UtbetalingsPostDto(Periode periode, InntektspostType inntektspostType) {
        Objects.requireNonNull(periode, "periode");
        Objects.requireNonNull(inntektspostType, "inntektspostType");
        this.periode = periode;
        this.inntektspostType = inntektspostType;
    }

    public InntektspostType getInntektspostType() {
        return inntektspostType;
    }

    public SkatteOgAvgiftsregelType getSkattAvgiftType() {
        return skattAvgiftType;
    }

    public void setSkattAvgiftType(SkatteOgAvgiftsregelType skattAvgiftType) {
        this.skattAvgiftType = skattAvgiftType;
    }

    public void setUtbetaltYtelseType(UtbetaltYtelseType ytelseType) {
        this.ytelseType = ytelseType == null ? null : new WrapUtbetaltYtelse(ytelseType.getKode(), ytelseType.getKodeverk());
    }

    public UtbetalingsPostDto medUtbetaltYtelseType(UtbetaltYtelseType ytelseType) {
        setUtbetaltYtelseType(ytelseType);
        return this;
    }

    public UtbetalingsPostDto medSkattAvgiftType(SkatteOgAvgiftsregelType skattAvgiftType) {
        setSkattAvgiftType(SkatteOgAvgiftsregelType.UDEFINERT.equals(skattAvgiftType) ? null : skattAvgiftType);
        return this;
    }

    public Periode getPeriode() {
        return periode;
    }

    public BigDecimal getBeløp() {
        return beløp;
    }

    public void setBeløp(BigDecimal beløp) {
        Objects.requireNonNull(beløp);
        this.beløp = beløp.setScale(2, RoundingMode.HALF_UP);
    }

    public UtbetalingsPostDto medBeløp(BigDecimal beløp) {
        setBeløp(beløp);
        return this;
    }

    public UtbetalingsPostDto medBeløp(int beløp) {
        setBeløp(BigDecimal.valueOf(beløp));
        return this;
    }

    public UtbetaltYtelseType getYtelseType() {
        if (ytelseType == null) {
            return null;
        }
        UtbetaltYtelseType utbetaltYtelseType = ytelseType.getUtbetaltYtelseType();
        return utbetaltYtelseType == null || utbetaltYtelseType.erUdefinert() ? null : ytelseType.getUtbetaltYtelseType();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var other = this.getClass().cast(obj);

        return Objects.equals(inntektspostType, other.inntektspostType) && Objects.equals(periode, other.periode) && Objects.equals(ytelseType,
            other.ytelseType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inntektspostType, periode, ytelseType);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
    static class WrapUtbetaltYtelse {

        @JsonProperty(value = "kode", required = true)
        @NotNull
        @Size(max = 50)
        @Pattern(regexp = "^[\\p{Alnum}ÆØÅæøå_\\.\\-]+$", message = "[${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
        private String kode;

        @JsonProperty(value = "kodeverk", required = true)
        @NotNull
        @Size(max = 50)
        @Pattern(regexp = "^[\\p{Alnum}ÆØÅæøå_\\\\.\\\\-]+$", message = "[${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
        private String kodeverk;

        @JsonCreator
        WrapUtbetaltYtelse(@JsonProperty(value = "kode", required = true) @NotNull String kode,
                           @JsonProperty(value = "kodeverk", required = true) @NotNull String kodeverk) {
            this.kode = kode;
            this.kodeverk = kodeverk;
        }

        UtbetaltYtelseType getUtbetaltYtelseType() {
            return UtbetaltYtelseType.getUtbetaltYtelseType(kode, kodeverk);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var other = (WrapUtbetaltYtelse) obj;

            return Objects.equals(kode, other.kode) && Objects.equals(kodeverk, other.kodeverk);
        }

        @Override
        public int hashCode() {
            return Objects.hash(kode, kodeverk);
        }

        @Override
        public String toString() {
            return "ytelseType<" + kode + ", " + kodeverk + ">";
        }

    }

}
