package no.nav.foreldrepenger.abakus.registerdata.ytelse.dpsak;

import java.util.Set;

import no.nav.fpsak.tidsserie.LocalDateInterval;

public record DpsakVedtak(LocalDateInterval periode, Integer dagsats, Set<DpsakUtbetaling> utbetalinger) {

    public record DpsakUtbetaling(LocalDateInterval periode, Integer dagsats, Integer utbetaltBeløp, Integer sumUtbetalt) {}

}
