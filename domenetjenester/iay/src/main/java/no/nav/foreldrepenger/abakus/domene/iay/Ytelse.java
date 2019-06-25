package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Collection;
import java.util.Optional;

import no.nav.foreldrepenger.abakus.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.Fagsystem;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface Ytelse {

    YtelseType getRelatertYtelseType();

    TemaUnderkategori getBehandlingsTema();

    YtelseStatus getStatus();

    DatoIntervallEntitet getPeriode();

    Saksnummer getSaksnummer();

    Fagsystem getKilde();

    Optional<YtelseGrunnlag> getYtelseGrunnlag();

    Collection<YtelseAnvist> getYtelseAnvist();

    Long getId();
}
