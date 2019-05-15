package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Optional;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;


public interface YtelseStørrelse {

    Optional<OrgNummer> getVirksomhet();

    Beløp getBeløp();

    InntektPeriodeType getHyppighet();
}
