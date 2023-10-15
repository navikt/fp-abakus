package no.nav.abakus.iaygrunnlag.ytelse.v1;

import java.math.BigDecimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.iaygrunnlag.Aktør;
import no.nav.abakus.iaygrunnlag.kodeverk.Inntektskategori;

/**
 * Angir størrelse for ytelse på arbeidsforholdnivå.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.ALWAYS)
public class AnvistAndelDto {

    /**
     * Kan være null.
     */
    @JsonProperty(value = "arbeidsgiver")
    @Valid
    private Aktør arbeidsgiver;

    @JsonProperty(value = "arbeidsforholdId")
    @Valid
    private String arbeidsforholdId;

    @JsonProperty(value = "dagsats", required = true)
    @DecimalMin(value = "0.00", message = "beløp [${validatedValue}] må være >= {value}")
    private BigDecimal dagsats;

    @JsonProperty("utbetalingsgrad")
    @DecimalMin(value = "0.00", message = "prosentsats [${validatedValue}] må være >= {value}")
    @DecimalMax(value = "100.00", message = "prosentsats [${validatedValue}] må være <= {value}")
    private BigDecimal utbetalingsgrad;

    // Andel av dagsats som utbetales til arbeidsgiver
    @JsonProperty("refusjonsgrad")
    @DecimalMin(value = "0.00", message = "prosentsats [${validatedValue}] må være >= {value}")
    @DecimalMax(value = "100.00", message = "prosentsats [${validatedValue}] må være <= {value}")
    private BigDecimal refusjonsgrad;

    @JsonProperty(value = "inntektskategori")
    @Valid
    private Inntektskategori inntektskategori;

    protected AnvistAndelDto() {
    }


    public AnvistAndelDto(Aktør arbeidsgiver,
                          String arbeidsforholdId,
                          BigDecimal dagsats,
                          BigDecimal utbetalingsgrad,
                          BigDecimal refusjonsgrad,
                          Inntektskategori inntektskategori) {
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdId = arbeidsforholdId;
        this.dagsats = dagsats;
        this.utbetalingsgrad = utbetalingsgrad;
        this.refusjonsgrad = refusjonsgrad;
        this.inntektskategori = inntektskategori;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public BigDecimal getDagsats() {
        return dagsats;
    }

    public BigDecimal getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

    public BigDecimal getRefusjonsgrad() {
        return refusjonsgrad;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }
}
