package no.nav.abakus.vedtak.ytelse.v1.anvisning;

import java.math.BigDecimal;
import java.util.Optional;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.vedtak.ytelse.Desimaltall;

/**
 * Angir størrelse for ytelse.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class AnvistAndel {

    /**
     * Kan være null.
     */
    @JsonProperty(value = "arbeidsgiverIdent")
    @Valid
    private ArbeidsgiverIdent arbeidsgiverIdent;

    @JsonProperty(value = "arbeidsforholdId")
    @Valid
    private String arbeidsforholdId;

    @JsonProperty(value = "dagsats", required = true)
    private Desimaltall dagsats;

    @JsonProperty("utbetalingsgrad")
    private Desimaltall utbetalingsgrad;

    // Andel av dagsats som utbetales til arbeidsgiver
    @JsonProperty("refusjonsgrad")
    private Desimaltall refusjonsgrad;

    @JsonProperty(value = "inntektklasse")
    @Valid
    private Inntektklasse inntektklasse;

    protected AnvistAndel() {
    }

    public AnvistAndel(ArbeidsgiverIdent arbeidsgiverIdent,
                       int beløp,
                       int utbetalingsgrad,
                       int refusjonsgrad,
                       Inntektklasse inntektklasse,
                       String arbeidsforholdId) {
        this(arbeidsgiverIdent, arbeidsforholdId, new Desimaltall(BigDecimal.valueOf(beløp)), new Desimaltall(BigDecimal.valueOf(utbetalingsgrad)),
            new Desimaltall(BigDecimal.valueOf(refusjonsgrad)), inntektklasse);
    }

    public AnvistAndel(ArbeidsgiverIdent arbeidsgiverIdent,
                       String arbeidsforholdId,
                       Desimaltall beløp,
                       Desimaltall utbetalingsgrad,
                       Desimaltall refusjonsgrad,
                       Inntektklasse inntektklasse) {
        this.arbeidsgiverIdent = arbeidsgiverIdent;
        this.arbeidsforholdId = arbeidsforholdId;
        this.dagsats = beløp;
        this.utbetalingsgrad = utbetalingsgrad;
        this.refusjonsgrad = refusjonsgrad;
        this.inntektklasse = inntektklasse;
    }

    public ArbeidsgiverIdent getArbeidsgiverIdent() {
        return arbeidsgiverIdent;
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public Desimaltall getDagsats() {
        return dagsats;
    }

    public Desimaltall getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

    public Desimaltall getRefusjonsgrad() {
        return refusjonsgrad;
    }

    public Inntektklasse getInntektklasse() {
        return Optional.ofNullable(inntektklasse).orElse(Inntektklasse.INGEN);
    }

}
