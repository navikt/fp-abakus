package no.nav.foreldrepenger.abakus.vedtak.domene;

import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.abakus.behandling.Fagsystem;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class VedtakYtelseBuilder {

    private final VedtakYtelseEntitet ytelse;

    private VedtakYtelseBuilder(VedtakYtelseEntitet ytelseEntitet) {
        this.ytelse = ytelseEntitet;
    }

    private static VedtakYtelseBuilder ny() {
        return new VedtakYtelseBuilder(new VedtakYtelseEntitet());
    }

    private static VedtakYtelseBuilder oppdatere(Ytelse oppdatere) {
        return new VedtakYtelseBuilder((VedtakYtelseEntitet) oppdatere);
    }

    public static VedtakYtelseBuilder oppdatere(Optional<VedtakYtelseEntitet> oppdatere) {
        return oppdatere.map(VedtakYtelseBuilder::oppdatere).orElseGet(VedtakYtelseBuilder::ny);
    }

    public VedtakYtelseBuilder medYtelseType(RelatertYtelseType relatertYtelseType) {
        ytelse.setYtelseType(relatertYtelseType);
        return this;
    }

    public VedtakYtelseBuilder medStatus(RelatertYtelseTilstand relatertYtelseTilstand) {
        ytelse.setStatus(relatertYtelseTilstand);
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
    public Ytelse build() {
        return ytelse;
    }

    public YtelseAnvistBuilder getAnvistBuilder() {
        return YtelseAnvistBuilder.ny();
    }

    public void tilbakestillAnvisteYtelser() {
        ytelse.tilbakestillAnvisteYtelser();
    }

}
