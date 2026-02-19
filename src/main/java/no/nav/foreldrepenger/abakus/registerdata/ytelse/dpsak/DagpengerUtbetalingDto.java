package no.nav.foreldrepenger.abakus.registerdata.ytelse.dpsak;

import java.time.LocalDate;

public record DagpengerUtbetalingDto(LocalDate fraOgMed, LocalDate tilOgMed, DagpengerKilde kilde,
                                     Integer sats, Integer utbetaltBeløp, Integer gjenståendeDager) {
}

