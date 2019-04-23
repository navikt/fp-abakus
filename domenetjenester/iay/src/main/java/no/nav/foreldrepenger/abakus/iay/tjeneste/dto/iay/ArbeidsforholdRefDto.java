package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import no.nav.foreldrepenger.abakus.typer.Fagsystem;

public class ArbeidsforholdRefDto {

    @JsonIgnore
    private Map<Fagsystem, String> referanser = new HashMap<>();

    public ArbeidsforholdRefDto() {

    }

    public String getAaRegisterReferanse() {
        return referanser.getOrDefault(Fagsystem.AAREGISTERET, null);
    }

    public String getAbakusReferanse() {
        return referanser.getOrDefault(Fagsystem.FPABAKUS, null);
    }

    public void leggTilReferanse(Fagsystem fagsystem, String referanse) {
        Objects.requireNonNull(fagsystem, "Fagsystem");
        referanser.put(fagsystem, referanse);
    }
}
