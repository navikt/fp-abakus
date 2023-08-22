package no.nav.abakus.vedtak.ytelse.v1.anvisning;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.vedtak.ytelse.Desimaltall;
import no.nav.abakus.vedtak.ytelse.Periode;

public class Anvisning {

    @NotNull
    @Valid
    @JsonProperty("periode")
    private Periode periode;

    @JsonProperty("beløp")
    private Desimaltall beløp;

    @JsonProperty("dagsats")
    private Desimaltall dagsats;

    @JsonProperty("utbetalingsgrad")
    private Desimaltall utbetalingsgrad;

    @JsonProperty("andeler")
    private List<AnvistAndel> andeler = new ArrayList<>();

    public Anvisning() {
    }

    public Periode getPeriode() {
        return periode;
    }

    public void setPeriode(Periode periode) {
        this.periode = periode;
    }

    public Desimaltall getBeløp() {
        return beløp;
    }

    public void setBeløp(Desimaltall beløp) {
        this.beløp = beløp;
    }

    public Desimaltall getDagsats() {
        return dagsats;
    }

    public void setDagsats(Desimaltall dagsats) {
        this.dagsats = dagsats;
    }

    public Desimaltall getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

    public void setUtbetalingsgrad(Desimaltall utbetalingsgrad) {
        this.utbetalingsgrad = utbetalingsgrad;
    }

    public List<AnvistAndel> getAndeler() {
        return andeler;
    }

    public void setAndeler(List<AnvistAndel> andeler) {
        this.andeler = andeler;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[periode=" + periode + ", beløp=" + beløp + ", dagsats=" + dagsats + ", utbetalingsgrad="
            + utbetalingsgrad + "]";
    }
}
