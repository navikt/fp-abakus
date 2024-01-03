package no.nav.foreldrepenger.abakus.app.jackson;

import jakarta.validation.constraints.Pattern;

class Patternklasse {

    @Pattern(regexp = "[Aa]")
    private String fritekst;
}
