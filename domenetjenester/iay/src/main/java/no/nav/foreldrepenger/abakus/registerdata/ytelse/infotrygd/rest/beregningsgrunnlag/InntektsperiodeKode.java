package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum InntektsperiodeKode {
    @JsonEnumDefaultValue
    UKJENT,
    M,
    U,
    D,
    Å,
    F,
    X,
    Y
}
