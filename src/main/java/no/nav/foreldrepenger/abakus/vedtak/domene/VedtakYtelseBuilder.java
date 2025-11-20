package no.nav.foreldrepenger.abakus.vedtak.domene;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;

public class VedtakYtelseBuilder {

    private final VedtakYtelse ytelse;
    private final LocalDateTime originalVedtattTidspunkt;

    private VedtakYtelseBuilder(VedtakYtelse ytelseEntitet) {
        this.ytelse = ytelseEntitet;
        this.originalVedtattTidspunkt = ytelseEntitet.getVedtattTidspunkt() != null ? ytelseEntitet.getVedtattTidspunkt() : LocalDateTime.MIN;
    }

    private static VedtakYtelseBuilder ny() {
        return new VedtakYtelseBuilder(new VedtakYtelse());
    }

    private static VedtakYtelseBuilder oppdatere(VedtakYtelse oppdatere) {
        return new VedtakYtelseBuilder(new VedtakYtelse(oppdatere));
    }

    public static VedtakYtelseBuilder oppdatere(Optional<VedtakYtelse> oppdatere) {
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

    public VedtakYtelseBuilder medPeriode(IntervallEntitet intervallEntitet) {
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

    public VedtakYtelseBuilder medTilleggsopplysninger(String tilleggsopplysninger) {
        ytelse.setTilleggsopplysninger(tilleggsopplysninger);
        return this;
    }

    public VedtakYtelseBuilder leggTil(YtelseAnvistBuilder ytelseAnvist) {
        Objects.requireNonNull(ytelseAnvist, "ytelseAnvist");
        ytelse.leggTilYtelseAnvist(ytelseAnvist.build());
        return this;
    }

    // public for å kunne testes
    public VedtakYtelse build() {
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
        return originalVedtattTidspunkt != null && ytelse.getVedtattTidspunkt() != null && originalVedtattTidspunkt.isBefore(
            ytelse.getVedtattTidspunkt());
    }
}
