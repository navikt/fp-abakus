package no.nav.foreldrepenger.abakus.registerdata;

import java.time.LocalDate;

public record ArenaRequestDto(String ident, LocalDate fom, LocalDate tom) {
}
