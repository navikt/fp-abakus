package no.nav.foreldrepenger.abakus.iay.tjeneste.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.PeriodeDto;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class ArbeidsforholdForAktørIPeriodeDto implements AbacDto {

    @NotNull
    @Valid
    private AktørDto aktør;
    @NotNull
    @Valid
    private PeriodeDto periode;

    public ArbeidsforholdForAktørIPeriodeDto() {
    }

    public AktørDto getAktør() {
        return aktør;
    }

    public void setAktør(AktørDto aktør) {
        this.aktør = aktør;
    }

    public PeriodeDto getPeriode() {
        return periode;
    }

    public void setPeriode(PeriodeDto periode) {
        this.periode = periode;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTilAktørId(aktør.getId());
    }
}
