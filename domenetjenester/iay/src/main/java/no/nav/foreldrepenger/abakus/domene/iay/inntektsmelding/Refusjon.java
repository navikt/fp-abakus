package no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding;

import java.time.LocalDate;

import no.nav.foreldrepenger.abakus.typer.Beløp;

public interface Refusjon {

    Beløp getRefusjonsbeløp();

    LocalDate getFom();
}
