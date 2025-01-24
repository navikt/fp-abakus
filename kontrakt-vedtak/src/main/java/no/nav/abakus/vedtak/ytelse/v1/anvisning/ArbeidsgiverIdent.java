package no.nav.abakus.vedtak.ytelse.v1.anvisning;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// Orgnr / aktørid
public record ArbeidsgiverIdent(@NotNull @JsonProperty("ident") @Pattern(regexp = "\\d{9}|\\d{13}") String ident) {

    public boolean erOrganisasjon() {
        return ident().length() == 9;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[verdi=MASKERT]";
    }
}
