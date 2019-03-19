package no.nav.foreldrepenger.abakus.iay.tjeneste.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class HentArbeidsforholdForReferanseDto implements AbacDto {

    @NotNull
    @Valid
    private ReferanseDto referanseDto;
    @Valid
    private AktørDto aktør;

    public HentArbeidsforholdForReferanseDto(ReferanseDto referanseDto, AktørDto aktør) {
        this.referanseDto = referanseDto;
        this.aktør = aktør;
    }

    public HentArbeidsforholdForReferanseDto() {
    }

    public ReferanseDto getReferanseDto() {
        return referanseDto;
    }

    public void setReferanseDto(ReferanseDto referanseDto) {
        this.referanseDto = referanseDto;
    }

    public AktørDto getAktør() {
        return aktør;
    }

    public void setAktør(AktørDto aktør) {
        this.aktør = aktør;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett();
    }
}
