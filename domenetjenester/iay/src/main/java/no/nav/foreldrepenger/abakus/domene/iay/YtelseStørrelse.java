package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Optional;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.domene.virksomhet.Virksomhet;


public interface YtelseStørrelse {

    Optional<Virksomhet> getVirksomhet();

    Beløp getBeløp();

    InntektPeriodeType getHyppighet();
}
