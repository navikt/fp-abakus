package no.nav.abakus.vedtak.ytelse;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class Aktør {

    @NotNull
    @JsonProperty("verdi")
    @Pattern(regexp = "\\d{11}|\\d{13}") // Fnr / aktørid
    private String verdi;

    public Aktør() {
    }

    public String getVerdi() {
        return verdi;
    }

    public void setVerdi(String verdi) {
        this.verdi = verdi;
    }

    public boolean erAktørId() {
        return verdi.length() == 13;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[verdi=MASKERT]";
    }
}
