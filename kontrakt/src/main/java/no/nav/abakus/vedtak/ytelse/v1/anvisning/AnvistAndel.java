package no.nav.abakus.vedtak.ytelse.v1.anvisning;

import java.math.BigDecimal;
import java.util.Optional;

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

    @JsonProperty(value = "inntektklasse")
    @Valid
    private Inntektklasse inntektklasse;

    protected AnvistAndel() {
    }

    @Deprecated(forRemoval = true)
    public AnvistAndel(Aktør arbeidsgiver, int beløp, int utbetalingsgrad, int refusjonsgrad, Inntektskategori inntektskategori, String arbeidsforholdId) {
        this(arbeidsgiver,
            arbeidsforholdId, new Desimaltall(BigDecimal.valueOf(beløp)),
            new Desimaltall(BigDecimal.valueOf(utbetalingsgrad)),
            new Desimaltall(BigDecimal.valueOf(refusjonsgrad)),
            inntektskategori);
    }

    public AnvistAndel(Aktør arbeidsgiver, int beløp, int utbetalingsgrad, int refusjonsgrad, Inntektklasse inntektklasse, String arbeidsforholdId) {
        this(arbeidsgiver,
            arbeidsforholdId, new Desimaltall(BigDecimal.valueOf(beløp)),
            new Desimaltall(BigDecimal.valueOf(utbetalingsgrad)),
            new Desimaltall(BigDecimal.valueOf(refusjonsgrad)),
            inntektklasse);
    }

    @Deprecated(forRemoval = true)
    public AnvistAndel(Aktør arbeidsgiver, String arbeidsforholdId, Desimaltall beløp, Desimaltall utbetalingsgrad, Desimaltall refusjonsgrad, Inntektskategori inntektskategori) {
        this(arbeidsgiver, arbeidsforholdId, beløp, utbetalingsgrad, refusjonsgrad, inntektskategori, fraInntektskategori(inntektskategori));
    }

    public AnvistAndel(Aktør arbeidsgiver, String arbeidsforholdId, Desimaltall beløp, Desimaltall utbetalingsgrad, Desimaltall refusjonsgrad, Inntektklasse inntektklasse) {
        this(arbeidsgiver, arbeidsforholdId, beløp, utbetalingsgrad, refusjonsgrad, fraInntektklasse(inntektklasse), inntektklasse);
    }

    @Deprecated(forRemoval = true)
    public AnvistAndel(Aktør arbeidsgiver, String arbeidsforholdId, Desimaltall beløp, Desimaltall utbetalingsgrad, Desimaltall refusjonsgrad, Inntektskategori inntektskategori, Inntektklasse inntektklasse) {
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdId = arbeidsforholdId;
        this.dagsats = beløp;
        this.utbetalingsgrad = utbetalingsgrad;
        this.refusjonsgrad = refusjonsgrad;
        this.inntektskategori = inntektskategori;
        this.inntektklasse = inntektklasse;
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

    @Deprecated(forRemoval = true)
    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public Inntektklasse getInntektklasse() {
        return Optional.ofNullable(inntektklasse)
            .or(() -> Optional.ofNullable(inntektskategori).map(AnvistAndel::fraInntektskategori))
            .orElse(Inntektklasse.INGEN);
    }

    @Deprecated(forRemoval = true)
    public static Inntektklasse fraInntektskategori(Inntektskategori inntektskategori) {
        return switch (inntektskategori) {
            case ARBEIDSTAKER -> Inntektklasse.ARBEIDSTAKER;
            case ARBEIDSTAKER_UTEN_FERIEPENGER -> Inntektklasse.ARBEIDSTAKER_UTEN_FERIEPENGER;
            case FRILANSER -> Inntektklasse.FRILANSER;
            case SELVSTENDIG_NÆRINGSDRIVENDE -> Inntektklasse.SELVSTENDIG_NÆRINGSDRIVENDE;
            case DAGPENGER -> Inntektklasse.DAGPENGER;
            case ARBEIDSAVKLARINGSPENGER -> Inntektklasse.ARBEIDSAVKLARINGSPENGER;
            case SJØMANN -> Inntektklasse.MARITIM;
            case DAGMAMMA -> Inntektklasse.DAGMAMMA;
            case JORDBRUKER -> Inntektklasse.JORDBRUKER;
            case FISKER -> Inntektklasse.FISKER;
            default -> Inntektklasse.INGEN;
        };
    }

    @Deprecated(forRemoval = true)
    public static Inntektskategori fraInntektklasse(Inntektklasse inntektklasse) {
        return switch (inntektklasse) {
            case ARBEIDSTAKER -> Inntektskategori.ARBEIDSTAKER;
            case ARBEIDSTAKER_UTEN_FERIEPENGER -> Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER;
            case FRILANSER -> Inntektskategori.FRILANSER;
            case SELVSTENDIG_NÆRINGSDRIVENDE -> Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE;
            case DAGPENGER -> Inntektskategori.DAGPENGER;
            case ARBEIDSAVKLARINGSPENGER -> Inntektskategori.ARBEIDSAVKLARINGSPENGER;
            case MARITIM -> Inntektskategori.SJØMANN;
            case DAGMAMMA -> Inntektskategori.DAGMAMMA;
            case JORDBRUKER -> Inntektskategori.JORDBRUKER;
            case FISKER -> Inntektskategori.FISKER;
            default -> Inntektskategori.UDEFINERT;
        };
    }
}
