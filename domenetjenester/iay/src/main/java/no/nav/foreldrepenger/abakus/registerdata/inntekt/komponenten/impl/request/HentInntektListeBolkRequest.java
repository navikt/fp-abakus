package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.request;

import java.time.YearMonth;
import java.util.List;

import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon.Aktoer;

public class HentInntektListeBolkRequest {
    private List<Aktoer> identListe;
    private String ainntektsfilter;
    private String formaal;
    private YearMonth maanedFom;
    private YearMonth maanedTom;

    public List<Aktoer> getIdentListe() {
        return identListe;
    }

    public void setIdentListe(List<Aktoer> identListe) {
        this.identListe = identListe;
    }

    public String getAinntektsfilter() {
        return ainntektsfilter;
    }

    public void setAinntektsfilter(String ainntektsfilter) {
        this.ainntektsfilter = ainntektsfilter;
    }

    public String getFormaal() {
        return formaal;
    }

    public void setFormaal(String formaal) {
        this.formaal = formaal;
    }

    public YearMonth getMaanedFom() {
        return maanedFom;
    }

    public void setMaanedFom(YearMonth maanedFom) {
        this.maanedFom = maanedFom;
    }

    public YearMonth getMaanedTom() {
        return maanedTom;
    }

    public void setMaanedTom(YearMonth maanedTom) {
        this.maanedTom = maanedTom;
    }
}
