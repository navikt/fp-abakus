package no.nav.foreldrepenger.abakus.registerdata.tjeneste.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class Aktør implements AbacDto {
    @NotNull
    @Pattern(regexp = "[\\d]{11}|[\\d]{13}")
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
