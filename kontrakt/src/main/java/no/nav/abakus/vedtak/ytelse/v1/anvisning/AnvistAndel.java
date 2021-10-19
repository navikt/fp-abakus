package no.nav.abakus.vedtak.ytelse.v1.anvisning;

import java.math.BigDecimal;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.iaygrunnlag.Aktør;
import no.nav.abakus.iaygrunnlag.kodeverk.Inntektskategori;
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
    @JsonProperty(value = "arbeidsgiver")
    @Valid
    private Aktør arbeidsgiver;

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

    @JsonProperty(value = "inntektskategori")
    @Valid
    private Inntektskategori inntektskategori;

    protected AnvistAndel() {
    }

    public AnvistAndel(Aktør arbeidsgiver, int beløp, int utbetalingsgrad, int refusjonsgrad, Inntektskategori inntektskategori, String arbeidsforholdId) {
        this(arbeidsgiver,
            arbeidsforholdId, new Desimaltall(BigDecimal.valueOf(beløp)),
            new Desimaltall(BigDecimal.valueOf(utbetalingsgrad)),
            new Desimaltall(BigDecimal.valueOf(refusjonsgrad)),
            inntektskategori);
    }

    public AnvistAndel(Aktør arbeidsgiver, String arbeidsforholdId, Desimaltall beløp, Desimaltall utbetalingsgrad, Desimaltall refusjonsgrad, Inntektskategori inntektskategori) {
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdId = arbeidsforholdId;
        this.dagsats = beløp;
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

    public Desimaltall getDagsats() {
        return dagsats;
    }

    public Desimaltall getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

    public Desimaltall getRefusjonsgrad() {
        return refusjonsgrad;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }
}
