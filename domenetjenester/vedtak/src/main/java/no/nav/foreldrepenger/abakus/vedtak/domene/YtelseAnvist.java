package no.nav.foreldrepenger.abakus.vedtak.domene;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;


public interface YtelseAnvist {

    LocalDate getAnvistFOM();

    LocalDate getAnvistTOM();

    Optional<Beløp> getBeløp();

    Optional<Beløp> getDagsats();

    Optional<Stillingsprosent> getUtbetalingsgradProsent();
}
