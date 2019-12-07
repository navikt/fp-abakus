package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum TemaKode {
    @JsonEnumDefaultValue
    UKJENT,
    FA,
    SP,
    BS
}
