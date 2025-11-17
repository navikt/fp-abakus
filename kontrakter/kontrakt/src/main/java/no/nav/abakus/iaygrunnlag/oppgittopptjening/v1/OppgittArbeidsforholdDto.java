package no.nav.abakus.iaygrunnlag.oppgittopptjening.v1;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidType;
import no.nav.abakus.iaygrunnlag.kodeverk.Landkode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OppgittArbeidsforholdDto {

    @JsonProperty(value = "periode", required = true)
    @Valid
    @NotNull
    private Periode periode;

    @JsonProperty(value = "arbeidType", required = true)
    @NotNull
    private ArbeidType arbeidType;

    @JsonProperty(value = "erUtenlandskInntekt")
    private Boolean erUtenlandskInntekt;

    @JsonProperty(value = "landkode", required = true)
    @Valid
    @NotNull
    private Landkode landkode = Landkode.NOR;

    /**
     * Tillater kun positive verdier.
     */
    @JsonProperty("inntekt")
    @DecimalMin(value = "0.00", message = "beløp [${validatedValue}] må være >= {value}")
    @DecimalMax(value = "9999999999.00", message = "beløp [${validatedValue}] må være >= {value}")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal inntekt;

    /**
     * Oppgis normalt dersom ikke orgnr kan gis. F.eks for utlandske virsomheter, eller noen tilfeller Fiskere med Lott.
     */
    @JsonProperty(value = "virksomhetNavn")
    @Pattern(regexp = "^[\\p{Graph}\\p{Space}\\p{Sc}\\p{L}\\p{M}\\p{N}]+$", message = "Oppgitt Arbeidsforhold - Virksomhet navn [${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
    private String virksomhetNavn;

    @JsonCreator
    public OppgittArbeidsforholdDto(@JsonProperty(value = "periode", required = true) Periode periode,
                                    @JsonProperty(value = "arbeidType", required = true) ArbeidType arbeidType) {
        Objects.requireNonNull(periode, "periode");
        Objects.requireNonNull(arbeidType, "arbeidType");
        this.periode = periode;
        this.arbeidType = arbeidType;
    }

    protected OppgittArbeidsforholdDto() {
        // default ctor
    }

    public Periode getPeriode() {
        return periode;
    }

    public Boolean isErUtenlandskInntekt() {
        return erUtenlandskInntekt;
    }

    public OppgittArbeidsforholdDto medErUtenlandskInntekt(boolean erUtenlandskInntekt) {
        setErUtenlandskInntekt(erUtenlandskInntekt);
        return this;
    }

    public OppgittArbeidsforholdDto medInntekt(BigDecimal inntekt) {
        setInntekt(inntekt);
        return this;
    }

    public ArbeidType getArbeidTypeDto() {
        return arbeidType;
    }

    public Boolean getErUtenlandskInntekt() {
        return erUtenlandskInntekt;
    }

    public void setErUtenlandskInntekt(boolean erUtenlandskInntekt) {
        this.erUtenlandskInntekt = erUtenlandskInntekt;
    }

    public void setErUtenlandskInntekt(Boolean erUtenlandskInntekt) {
        this.erUtenlandskInntekt = erUtenlandskInntekt;
    }

    public Landkode getLandkode() {
        return landkode;
    }

    public void setLandkode(Landkode landkode) {
        this.landkode = landkode;
    }

    public String getVirksomhetNavn() {
        return virksomhetNavn;
    }

    public void setVirksomhetNavn(String virksomhetNavn) {
        this.virksomhetNavn = virksomhetNavn;
    }

    public OppgittArbeidsforholdDto medOppgittVirksomhetNavn(String virksomhetNavn, Landkode landkode) {
        setLandkode(landkode);
        setVirksomhetNavn(virksomhetNavn);
        return this;
    }

    public BigDecimal getInntekt() {
        return inntekt;
    }

    public void setInntekt(BigDecimal inntekt) {
        this.inntekt = inntekt;
    }
}
