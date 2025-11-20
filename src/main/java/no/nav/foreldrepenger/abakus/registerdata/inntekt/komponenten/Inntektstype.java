package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten;

import com.fasterxml.jackson.annotation.JsonValue;

/*
 * Inntektstyper fra kilden
 * Disse har hvert sitt sett med mulige beskrivelser - noen er med i InntektYtelseType for gjenkjenning
 */
public enum Inntektstype {

    LØNN("Loennsinntekt"),
    YTELSE("YtelseFraOffentlige"),
    NÆRING("Naeringsinntekt"),
    TRYGD("PensjonEllerTrygd");

    @JsonValue
    private final String type;

    Inntektstype(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
