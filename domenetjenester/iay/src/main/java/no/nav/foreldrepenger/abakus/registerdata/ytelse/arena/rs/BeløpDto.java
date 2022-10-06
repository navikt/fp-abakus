package no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.rs;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record BeløpDto(@JsonValue BigDecimal verdi) {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public BeløpDto { // NOSONAR
    }

    @Override
    public BigDecimal verdi() {
        return verdi;
    }
}
