package no.nav.foreldrepenger.abakus.vedtak.domene;

import java.util.Collection;

import no.nav.foreldrepenger.abakus.behandling.Fagsystem;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface Ytelse {

    AktørId getAktør();

    RelatertYtelseType getYtelseType();

    TemaUnderkategori getBehandlingsTema();

    RelatertYtelseTilstand getStatus();

    DatoIntervallEntitet getPeriode();

    Saksnummer getSaksnummer();

    Fagsystem getKilde();

    Collection<YtelseAnvist> getYtelseAnvist();
}
