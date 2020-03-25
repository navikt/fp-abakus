package no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding;

import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.Beløp;

public interface NaturalYtelse {

    IntervallEntitet getPeriode();

    Beløp getBeloepPerMnd();

    NaturalYtelseType getType();
}
