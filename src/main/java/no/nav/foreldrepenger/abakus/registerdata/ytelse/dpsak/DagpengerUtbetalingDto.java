package no.nav.foreldrepenger.abakus.registerdata.ytelse.dpsak;

import java.time.LocalDate;

// Kommer i perioder på 1 dag fra kilde DP-sak og 14 dager fra Arena
public record DagpengerUtbetalingDto(LocalDate fraOgMed, LocalDate tilOgMed, DagpengerKilde kilde,
                                     Integer sats, Integer utbetaltBeløp, Integer gjenståendeDager) {
}

