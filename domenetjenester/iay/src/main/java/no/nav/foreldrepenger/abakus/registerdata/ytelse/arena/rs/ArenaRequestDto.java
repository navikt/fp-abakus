package no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.rs;

import java.time.LocalDate;

public record ArenaRequestDto(String ident, LocalDate fom, LocalDate tom) {
}
