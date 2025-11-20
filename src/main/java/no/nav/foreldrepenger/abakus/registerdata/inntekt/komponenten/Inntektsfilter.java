package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Inntektsfilter {

    OPPTJENINGSGRUNNLAG("PensjonsgivendeA-Inntekt"),
    BEREGNINGSGRUNNLAG("8-28"),
    SAMMENLIGNINGSGRUNNLAG("8-30");

    @JsonValue
    private final String filter;

    Inntektsfilter(String filter) {
        this.filter = filter;
    }

    public String getFilter() {
        return filter;
    }

}
