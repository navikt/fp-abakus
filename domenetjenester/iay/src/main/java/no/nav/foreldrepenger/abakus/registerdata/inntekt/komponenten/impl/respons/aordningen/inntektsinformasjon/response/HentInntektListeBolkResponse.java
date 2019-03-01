package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon.response;

import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon.ArbeidsInntektIdent;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon.Sikkerhetsavvik;

public class HentInntektListeBolkResponse {
    private List<ArbeidsInntektIdent> arbeidsInntektIdentListe = new ArrayList<>();
    private List<Sikkerhetsavvik> sikkerhetsavvikListe = new ArrayList<>();

    public HentInntektListeBolkResponse() {
    }

    public HentInntektListeBolkResponse(List<ArbeidsInntektIdent> arbeidsInntektIdentListe, List<Sikkerhetsavvik> sikkerhetsavvikListe) {
        this.arbeidsInntektIdentListe = arbeidsInntektIdentListe;
        this.sikkerhetsavvikListe = sikkerhetsavvikListe;
    }

    public List<ArbeidsInntektIdent> getArbeidsInntektIdentListe() {
        return this.arbeidsInntektIdentListe;
    }

    public void setArbeidsInntektIdentListe(List<ArbeidsInntektIdent> arbeidsInntektIdentListe) {
        this.arbeidsInntektIdentListe = arbeidsInntektIdentListe;
    }

    public List<Sikkerhetsavvik> getSikkerhetsavvikListe() {
        return this.sikkerhetsavvikListe;
    }

    public void setSikkerhetsavvikListe(List<Sikkerhetsavvik> sikkerhetsavvikListe) {
        this.sikkerhetsavvikListe = sikkerhetsavvikListe;
    }

}
