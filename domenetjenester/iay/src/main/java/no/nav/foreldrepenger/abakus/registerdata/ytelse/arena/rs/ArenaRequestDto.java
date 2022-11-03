package no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.rs;

import java.time.LocalDate;

public record ArenaRequestDto(String ident, LocalDate fom, LocalDate tom) {

    @Override
    public String toString() {
        return "ArenaRequestDto{" +
            "ident='***'" +
            ", fom=" + fom +
            ", tom=" + tom +
            '}';
    }
}
