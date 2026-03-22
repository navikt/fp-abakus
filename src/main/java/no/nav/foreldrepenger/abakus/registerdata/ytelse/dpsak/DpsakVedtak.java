package no.nav.foreldrepenger.abakus.registerdata.ytelse.dpsak;

import java.util.List;

import no.nav.fpsak.tidsserie.LocalDateInterval;

// Er konstruert fra dp-datadeling ved å slå sammen perioder med lik dagsats
public record DpsakVedtak(LocalDateInterval periode, Integer dagsats, List<DpsakUtbetaling> utbetalinger) {

    // Dagsats og dagutbetalt er sats og beløp pr dag - som skal være like for perioden. sumUtbetalt er sum av dagutbetalt for perioden
    public record DpsakUtbetaling(LocalDateInterval periode, Integer dagsats, Integer dagutbetalt, Integer sumUtbetalt) {}

}
