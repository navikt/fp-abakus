package no.nav.foreldrepenger.abakus.registerdata.tjeneste.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class InnhentRegisterdataDto implements AbacDto {

    @Valid
    @NotNull
    private ReferanseDto referanse;
    @Valid
    private Aktør aktørId;
    @Valid
    private Aktør annenPartAktørId;
    @NotNull
    @Valid
    private PeriodeDto opplysningsperiode;
    @Valid
    private PeriodeDto opptjeningsperiode;

    public InnhentRegisterdataDto() {
    }

    public String getReferanse() {
        return referanse.getReferanse();
    }

    public void setReferanse(ReferanseDto referanse) {
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
        AbacDataAttributter opprett = AbacDataAttributter.opprett();
        if (annenPartAktørId != null) {
            opprett.leggTil(annenPartAktørId.abacAttributter());
        }
        return opprett.leggTil(aktørId.abacAttributter());
    }
}
