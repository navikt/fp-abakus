package no.nav.foreldrepenger.abakus.iay.tjeneste.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class ReferanseDto implements AbacDto {

    @NotNull
    @Pattern(regexp = "[-|\\w|\\d]*")
    private String referanse;

    public ReferanseDto(String referanse) {
        this.referanse = referanse;
    }

    public ReferanseDto() {
    }

    public String getReferanse() {
        return referanse;
    }

    public void setReferanse(String referanse) {
        this.referanse = referanse;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett();
    }
}
