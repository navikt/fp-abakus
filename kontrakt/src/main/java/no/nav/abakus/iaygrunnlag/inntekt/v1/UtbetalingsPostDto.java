package no.nav.abakus.iaygrunnlag.inntekt.v1;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektYtelseType;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.abakus.iaygrunnlag.kodeverk.LønnsinntektBeskrivelse;
import no.nav.abakus.iaygrunnlag.kodeverk.SkatteOgAvgiftsregelType;

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

    @JsonProperty("lønnsinntektBeskrivelse")
    private LønnsinntektBeskrivelse lønnsinntektBeskrivelse;

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
    @JsonProperty(value = "inntektYtelseType")
    @Valid
    private InntektYtelseType inntektYtelseType;

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

    public LønnsinntektBeskrivelse getLønnsinntektBeskrivelse() {
        return lønnsinntektBeskrivelse;
    }

    public void setLønnsinntektBeskrivelse(LønnsinntektBeskrivelse lønnsinntektBeskrivelse) {
        this.lønnsinntektBeskrivelse = lønnsinntektBeskrivelse;
    }

    public UtbetalingsPostDto medSkattAvgiftType(SkatteOgAvgiftsregelType skattAvgiftType) {
        setSkattAvgiftType(SkatteOgAvgiftsregelType.UDEFINERT.equals(skattAvgiftType) ? null : skattAvgiftType);
        return this;
    }

    public UtbetalingsPostDto medLønnsinntektbeskrivelse(LønnsinntektBeskrivelse lønnsinntektBeskrivelse) {
        setLønnsinntektBeskrivelse(LønnsinntektBeskrivelse.UDEFINERT.equals(lønnsinntektBeskrivelse) ? null : lønnsinntektBeskrivelse);
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

    public InntektYtelseType getInntektYtelseType() {
        return inntektYtelseType;
    }

    public void setInntektYtelseType(InntektYtelseType ytelseType) {
        this.inntektYtelseType = ytelseType;
    }

    public UtbetalingsPostDto medInntektYtelseType(InntektYtelseType ytelseType) {
        setInntektYtelseType(ytelseType);
        return this;
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

        return Objects.equals(inntektspostType, other.inntektspostType) && Objects.equals(periode, other.periode) && Objects.equals(inntektYtelseType,
            other.inntektYtelseType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inntektspostType, periode, inntektYtelseType);
    }

}
