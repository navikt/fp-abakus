package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Optional;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.TemaUnderkategori;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;

public class YtelseBuilder {

    private final Ytelse ytelse;
    private final boolean oppdaterer;

    private YtelseBuilder(Ytelse ytelse, boolean oppdaterer) {
        this.ytelse = ytelse;
        this.oppdaterer = oppdaterer;
    }

    private static YtelseBuilder ny() {
        return new YtelseBuilder(new Ytelse(), false);
    }

    private static YtelseBuilder oppdatere(Ytelse oppdatere) {
        return new YtelseBuilder(oppdatere, true);
    }

    public static YtelseBuilder oppdatere(Optional<Ytelse> oppdatere) {
        return oppdatere.map(YtelseBuilder::oppdatere).orElseGet(YtelseBuilder::ny);
    }

    public YtelseBuilder medYtelseType(YtelseType relatertYtelseType) {
        ytelse.setRelatertYtelseType(relatertYtelseType);
        return this;
    }

    public YtelseBuilder medStatus(YtelseStatus ytelseStatus) {
        ytelse.setStatus(ytelseStatus);
        return this;
    }

    public YtelseBuilder medPeriode(IntervallEntitet intervallEntitet) {
        ytelse.setPeriode(intervallEntitet);
        return this;
    }

    public YtelseBuilder medSaksreferanse(Saksnummer sakId) {
        ytelse.setSaksreferanse(sakId);
        return this;
    }

    public YtelseBuilder medKilde(Fagsystem kilde) {
        ytelse.setKilde(kilde);
        return this;
    }

    public YtelseBuilder medYtelseGrunnlag(YtelseGrunnlag ytelseGrunnlag) {
        ytelse.setYtelseGrunnlag(ytelseGrunnlag);
        return this;
    }

    public YtelseBuilder leggtilYtelseAnvist(YtelseAnvist ytelseAnvist) {
        ytelse.leggTilYtelseAnvist(ytelseAnvist);
        return this;
    }

    public YtelseBuilder medBehandlingsTema(TemaUnderkategori behandlingsTema) {
        ytelse.setBehandlingsTema(behandlingsTema);
        return this;
    }

    public IntervallEntitet getPeriode() {
        return ytelse.getPeriode();
    }

    boolean getErOppdatering() {
        return this.oppdaterer;
    }

    public Ytelse build() {
        verifiserTemaOgUnderkategori(ytelse.getRelatertYtelseType(), ytelse.getBehandlingsTema());
        return ytelse;
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
        ytelse.tilbakestillAnvisteYtelser();
    }

    public YtelseGrunnlagBuilder getGrunnlagBuilder() {
        return YtelseGrunnlagBuilder.ny();
    }

}
