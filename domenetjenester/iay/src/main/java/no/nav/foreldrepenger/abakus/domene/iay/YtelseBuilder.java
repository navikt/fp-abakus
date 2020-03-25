package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Optional;

import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.Fagsystem;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.abakus.vedtak.domene.TemaUnderkategori;

public class YtelseBuilder {

    private final YtelseEntitet ytelseEntitet;
    private final boolean oppdaterer;

    private YtelseBuilder(YtelseEntitet ytelseEntitet, boolean oppdaterer) {
        this.ytelseEntitet = ytelseEntitet;
        this.oppdaterer = oppdaterer;
    }

    private static YtelseBuilder ny() {
        return new YtelseBuilder(new YtelseEntitet(), false);
    }

    private static YtelseBuilder oppdatere(Ytelse oppdatere) {
        return new YtelseBuilder((YtelseEntitet) oppdatere, true);
    }

    public static YtelseBuilder oppdatere(Optional<Ytelse> oppdatere) {
        return oppdatere.map(YtelseBuilder::oppdatere).orElseGet(YtelseBuilder::ny);
    }

    public YtelseBuilder medYtelseType(YtelseType relatertYtelseType) {
        ytelseEntitet.setRelatertYtelseType(relatertYtelseType);
        return this;
    }

    public YtelseBuilder medStatus(YtelseStatus ytelseStatus) {
        ytelseEntitet.setStatus(ytelseStatus);
        return this;
    }

    public YtelseBuilder medPeriode(IntervallEntitet intervallEntitet) {
        ytelseEntitet.setPeriode(intervallEntitet);
        return this;
    }

    public YtelseBuilder medSaksnummer(Saksnummer sakId) {
        ytelseEntitet.medSakId(sakId);
        return this;
    }

    public YtelseBuilder medKilde(Fagsystem kilde) {
        ytelseEntitet.setKilde(kilde);
        return this;
    }

    public YtelseBuilder medYtelseGrunnlag(YtelseGrunnlag ytelseGrunnlag) {
        ytelseEntitet.setYtelseGrunnlag(ytelseGrunnlag);
        return this;
    }

    public YtelseBuilder leggtilYtelseAnvist(YtelseAnvist ytelseAnvist) {
        ytelseEntitet.leggTilYtelseAnvist(ytelseAnvist);
        return this;
    }

    public YtelseBuilder medBehandlingsTema(TemaUnderkategori behandlingsTema) {
        ytelseEntitet.setBehandlingsTema(behandlingsTema);
        return this;
    }

    public IntervallEntitet getPeriode() {
        return ytelseEntitet.getPeriode();
    }

    boolean getErOppdatering() {
        return this.oppdaterer;
    }

    public Ytelse build() {
        verifiserTemaOgUnderkategori(ytelseEntitet.getRelatertYtelseType(), ytelseEntitet.getBehandlingsTema());
        return ytelseEntitet;
    }

    private void verifiserTemaOgUnderkategori(YtelseType relatertYtelseType, TemaUnderkategori behandlingsTema) {
        if (YtelseType.PÅRØRENDESYKDOM.equals(relatertYtelseType)) {
            // Må ha tema underkategori når ytelsetypen er pleiepenger
            if (TemaUnderkategori.UDEFINERT.equals(behandlingsTema)) {
                throw new IllegalStateException("Må han tema underkategori når yteletype er pleiepenger");
            }
        }
    }

    public YtelseAnvistBuilder getAnvistBuilder() {
        return YtelseAnvistBuilder.ny();
    }

    public void tilbakestillAnvisninger() {
        ytelseEntitet.tilbakestillAnvisteYtelser();
    }

    public YtelseGrunnlagBuilder getGrunnlagBuilder() {
        return YtelseGrunnlagBuilder.ny();
    }

}
