package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon;

import java.util.ArrayList;
import java.util.List;

public class ArbeidsInntektIdent {
    private List<ArbeidsInntektMaaned> arbeidsInntektMaaned = new ArrayList<>();
    private Aktoer ident;


    public List<ArbeidsInntektMaaned> getArbeidsInntektMaaned() {
        return this.arbeidsInntektMaaned;
    }

    public Aktoer getIdent() {
        return this.ident;
    }

    public void setArbeidsInntektMaaned(List<ArbeidsInntektMaaned> arbeidsInntektMaaned) {
        this.arbeidsInntektMaaned = arbeidsInntektMaaned;
    }

    public void setIdent(Aktoer ident) {
        this.ident = ident;
    }

    public ArbeidsInntektIdent() {
    }

    public ArbeidsInntektIdent(List<ArbeidsInntektMaaned> arbeidsInntektMaaned, Aktoer ident) {
        this.arbeidsInntektMaaned = arbeidsInntektMaaned;
        this.ident = ident;
    }
}
