package no.nav.foreldrepenger.abakus.registerdata.tjeneste.dto;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class InnhentRegisterdataDto implements AbacDto {
    private String referanse;
    private Aktør aktørId;
    private Aktør annenPartAktørId;
    private PeriodeDto opplysningsperiode;
    private PeriodeDto opptjeningsperiode;

    public InnhentRegisterdataDto() {
    }

    public String getReferanse() {
        return referanse;
    }

    public void setReferanse(String referanse) {
        this.referanse = referanse;
    }

    public Aktør getAktørId() {
        return aktørId;
    }

    public void setAktørId(Aktør aktørId) {
        this.aktørId = aktørId;
    }

    public Aktør getAnnenPartAktørId() {
        return annenPartAktørId;
    }

    public void setAnnenPartAktørId(Aktør annenPartAktørId) {
        this.annenPartAktørId = annenPartAktørId;
    }

    public PeriodeDto getOpplysningsperiode() {
        return opplysningsperiode;
    }

    public void setOpplysningsperiode(PeriodeDto opplysningsperiode) {
        this.opplysningsperiode = opplysningsperiode;
    }

    public PeriodeDto getOpptjeningsperiode() {
        return opptjeningsperiode;
    }

    public void setOpptjeningsperiode(PeriodeDto opptjeningsperiode) {
        this.opptjeningsperiode = opptjeningsperiode;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTilAktørId(aktørId.getId());
    }
}
