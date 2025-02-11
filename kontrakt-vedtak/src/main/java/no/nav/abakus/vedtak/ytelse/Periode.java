package no.nav.abakus.vedtak.ytelse;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class Periode {

    @NotNull
    @JsonProperty("fom")
    private LocalDate fom;
    @NotNull
    @JsonProperty("tom")
    private LocalDate tom;

    public Periode() {
    }

    public LocalDate getFom() {
        return fom;
    }

    public void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[fom=" + fom + ", tom=" + tom + "]";
    }
}
