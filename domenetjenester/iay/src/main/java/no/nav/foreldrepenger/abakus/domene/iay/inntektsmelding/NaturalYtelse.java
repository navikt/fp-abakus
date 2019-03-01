package no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding;

import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface NaturalYtelse {

    DatoIntervallEntitet getPeriode();

    Beløp getBeloepPerMnd();

    NaturalYtelseType getType();
}
