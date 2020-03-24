package no.nav.foreldrepenger.abakus.vedtak.domene;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Fagsystem;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;

public interface VedtattYtelse {

    AktørId getAktør();

    YtelseType getYtelseType();

    TemaUnderkategori getBehandlingsTema();

    YtelseStatus getStatus();

    IntervallEntitet getPeriode();

    Saksnummer getSaksnummer();

    UUID getVedtakReferanse();

    Fagsystem getKilde();

    Collection<YtelseAnvist> getYtelseAnvist();

    LocalDateTime getVedtattTidspunkt();
}
