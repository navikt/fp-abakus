package no.nav.foreldrepenger.abakus.iay.tjeneste.dto;

import java.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class ArbeidsforholdForAktørPåDatoDto implements AbacDto {

    @NotNull
    @Valid
    private AktørDto aktør;
    @NotNull
    @Valid
    private LocalDate dato;

    public ArbeidsforholdForAktørPåDatoDto() {
    }

    public AktørDto getAktør() {
        return aktør;
    }

    public void setAktør(AktørDto aktør) {
        this.aktør = aktør;
    }

    public LocalDate getDato() {
        return dato;
    }

    public void setDato(LocalDate dato) {
        this.dato = dato;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTilAktørId(aktør.getId());
    }
}
