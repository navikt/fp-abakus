package no.nav.foreldrepenger.abakus.app.exceptions;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class FeltFeilDto implements Serializable {

    private String navn;
    private String melding;
    private String metainformasjon;

    public FeltFeilDto(String navn, String melding) {
        this.navn = navn;
        this.melding = melding;
    }

    public FeltFeilDto(String navn, String melding, String metainformasjon) {
        this.navn = navn;
        this.melding = melding;
        this.metainformasjon = metainformasjon;
    }

    public String getNavn() {
        return navn;
    }

    public String getMelding() {
        return melding;
    }

    public String getMetainformasjon() {
        return metainformasjon;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return "FeltFeilDto{" +
            "navn='" + navn + '\'' +
            ", melding='" + melding + '\'' +
            ", metainformasjon='" + metainformasjon + '\'' +
            '}';
    }
}
