package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum BehandlingstemaKode {
    @JsonEnumDefaultValue
    UKJENT,
    AP,
    FP,
    FU,
    FØ,
    SV,
    SP,
    OM,
    PB,
    OP,
    PP,
    PI,
    PN
}
