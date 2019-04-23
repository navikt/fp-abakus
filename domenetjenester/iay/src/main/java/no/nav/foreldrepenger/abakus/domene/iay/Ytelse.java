package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Collection;
import java.util.Optional;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.FagsystemUnderkategori;
import no.nav.foreldrepenger.abakus.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.abakus.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.abakus.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.abakus.typer.Fagsystem;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface Ytelse {

    RelatertYtelseType getRelatertYtelseType();

    TemaUnderkategori getBehandlingsTema();

    RelatertYtelseTilstand getStatus();

    DatoIntervallEntitet getPeriode();

    Saksnummer getSaksnummer();

    Fagsystem getKilde();

    FagsystemUnderkategori getFagsystemUnderkategori();

    Optional<YtelseGrunnlag> getYtelseGrunnlag();

    Collection<YtelseAnvist> getYtelseAnvist();
}
