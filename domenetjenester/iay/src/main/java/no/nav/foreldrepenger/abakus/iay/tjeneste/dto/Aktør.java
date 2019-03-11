package no.nav.foreldrepenger.abakus.iay.tjeneste.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class Aktør implements AbacDto {
    @NotNull
    @Pattern(regexp = "")
    private String id;

    public Aktør() {
    }

    public Aktør(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTilAktørId(id);
    }
}
