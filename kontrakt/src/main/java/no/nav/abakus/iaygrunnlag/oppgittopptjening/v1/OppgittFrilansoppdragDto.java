package no.nav.abakus.iaygrunnlag.oppgittopptjening.v1;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.abakus.iaygrunnlag.Periode;

import java.math.BigDecimal;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OppgittFrilansoppdragDto {

    @JsonProperty(value = "periode", required = true)
    @NotNull
    @Valid
    private Periode periode;

    @JsonProperty(value = "oppdragsgiver")
    @Size(max = 100)
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "[${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
    private String oppdragsgiver;

    /**
     * Tillater kun positive verdier. Max verdi håndteres av mottager.
     */
    @JsonProperty("inntekt")
    @DecimalMin(value = "0.00", message = "beløp [${validatedValue}] må være >= {value}")
    private BigDecimal inntekt;

    protected OppgittFrilansoppdragDto() {
    }

    public OppgittFrilansoppdragDto(Periode periode, String oppdragsgiver) {
        Objects.requireNonNull(periode, "periode");
        this.periode = periode;
        this.oppdragsgiver = oppdragsgiver;
    }

    public String getOppdragsgiver() {
        return oppdragsgiver;
    }

    public Periode getPeriode() {
        return periode;
    }

    public OppgittFrilansoppdragDto medInntekt(BigDecimal inntekt) {
        this.inntekt = inntekt;
        return this;
    }

    public BigDecimal getInntekt() {
        return inntekt;
    }
}
