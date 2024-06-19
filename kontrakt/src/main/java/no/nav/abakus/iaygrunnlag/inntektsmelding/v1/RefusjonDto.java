package no.nav.abakus.iaygrunnlag.inntektsmelding.v1;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class RefusjonDto {

    @JsonProperty(value = "fom", required = true)
    @Valid
    @NotNull
    private LocalDate fom;

    /** Tillater kun positive verdier. Max verdi håndteres av mottager. */
    @JsonProperty(value = "refusjonBeløpPerMnd")
    @DecimalMin(value = "0.00", message = "beløp [${validatedValue}] må være >= {value}")
    private BigDecimal refusjonBeløpPerMnd;

    protected RefusjonDto() {}

    public RefusjonDto(LocalDate fom, BigDecimal refusjonBeløpPerMnd) {
        Objects.requireNonNull(fom, "fom");
        this.fom = fom;
        this.refusjonBeløpPerMnd =
                refusjonBeløpPerMnd == null ? null : refusjonBeløpPerMnd.setScale(2, RoundingMode.HALF_UP);
    }

    public RefusjonDto(LocalDate fom, int refusjonBeløpPerMnd) {
        this(fom, BigDecimal.valueOf(refusjonBeløpPerMnd));
    }

    public BigDecimal getRefusjonsbeløpMnd() {
        return refusjonBeløpPerMnd;
    }

    public LocalDate getFom() {
        return fom;
    }
}
