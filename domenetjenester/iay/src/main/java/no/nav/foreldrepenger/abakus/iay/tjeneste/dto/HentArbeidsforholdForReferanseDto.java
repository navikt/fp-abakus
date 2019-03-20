package no.nav.foreldrepenger.abakus.iay.tjeneste.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class HentArbeidsforholdForReferanseDto implements AbacDto {

    @NotNull
    @Valid
    private ReferanseDto referanseDto;

    public HentArbeidsforholdForReferanseDto() {
    }

    public ReferanseDto getReferanseDto() {
        return referanseDto;
    }

    public void setReferanseDto(ReferanseDto referanseDto) {
        this.referanseDto = referanseDto;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett();
    }
}
