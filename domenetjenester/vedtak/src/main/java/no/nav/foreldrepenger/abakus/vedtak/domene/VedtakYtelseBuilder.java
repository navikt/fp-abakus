package no.nav.foreldrepenger.abakus.vedtak.domene;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import no.nav.foreldrepenger.abakus.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Fagsystem;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class VedtakYtelseBuilder {

    private final VedtakYtelseEntitet ytelse;
    private final LocalDateTime originalVedtattTidspunkt;

    private VedtakYtelseBuilder(VedtakYtelseEntitet ytelseEntitet) {
        this.ytelse = ytelseEntitet;
        this.originalVedtattTidspunkt = ytelseEntitet.getVedtattTidspunkt() != null ? ytelseEntitet.getVedtattTidspunkt() : LocalDateTime.MIN;
    }

    private static VedtakYtelseBuilder ny() {
        return new VedtakYtelseBuilder(new VedtakYtelseEntitet());
    }

    private static VedtakYtelseBuilder oppdatere(VedtattYtelse oppdatere) {
        return new VedtakYtelseBuilder(new VedtakYtelseEntitet(oppdatere));
    }

    public static VedtakYtelseBuilder oppdatere(Optional<VedtakYtelseEntitet> oppdatere) {
        return oppdatere.map(VedtakYtelseBuilder::oppdatere).orElseGet(VedtakYtelseBuilder::ny);
    }

    public VedtakYtelseBuilder medYtelseType(YtelseType relatertYtelseType) {
        ytelse.setYtelseType(relatertYtelseType);
        return this;
    }

    public VedtakYtelseBuilder medVedtattTidspunkt(LocalDateTime vedtattTidspunkt) {
        ytelse.setVedtattTidspunkt(vedtattTidspunkt);
        return this;
    }

    public VedtakYtelseBuilder medVedtakReferanse(UUID uuid) {
        ytelse.setVedtakReferanse(uuid);
        return this;
    }

    public VedtakYtelseBuilder medStatus(YtelseStatus ytelseStatus) {
        ytelse.setStatus(ytelseStatus);
        return this;
    }

    public VedtakYtelseBuilder medAktør(AktørId aktørId) {
        ytelse.setAktørId(aktørId);
        return this;
    }

    public VedtakYtelseBuilder medPeriode(DatoIntervallEntitet intervallEntitet) {
        ytelse.setPeriode(intervallEntitet);
        return this;
    }

    public VedtakYtelseBuilder medSaksnummer(Saksnummer sakId) {
        ytelse.medSakId(sakId);
        return this;
    }

    public VedtakYtelseBuilder medKilde(Fagsystem kilde) {
        ytelse.setKilde(kilde);
        return this;
    }

    public VedtakYtelseBuilder leggTil(YtelseAnvistBuilder ytelseAnvist) {
        Objects.requireNonNull(ytelseAnvist, "ytelseAnvist");
        ytelse.leggTilYtelseAnvist(ytelseAnvist.build());
        return this;
    }

    public VedtakYtelseBuilder medBehandlingsTema(TemaUnderkategori behandlingsTema) {
        ytelse.setBehandlingsTema(behandlingsTema);
        return this;
    }

    // public for å kunne testes
    public VedtattYtelse build() {
        return ytelse;
    }

    public YtelseAnvistBuilder getAnvistBuilder() {
        return YtelseAnvistBuilder.ny();
    }

    public void tilbakestillAnvisteYtelser() {
        ytelse.tilbakestillAnvisteYtelser();
    }

    /**
     * Vurderer om vedtatt tidspunktet er etter det eksisterende slik at det oppdateres ved nyere vedtakstidspunkt
     *
     * @return true / false avhengig av tidsstempel
     */
    boolean erOppdatering() {
        return originalVedtattTidspunkt.isBefore(ytelse.getVedtattTidspunkt());
    }
}
