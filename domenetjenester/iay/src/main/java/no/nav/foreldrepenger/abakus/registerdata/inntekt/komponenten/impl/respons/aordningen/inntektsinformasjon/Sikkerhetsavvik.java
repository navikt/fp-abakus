package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon;

public class Sikkerhetsavvik {
    private Aktoer ident;
    private String tekst;

    public Aktoer getIdent() {
        return this.ident;
    }

    public String getTekst() {
        return this.tekst;
    }

    public void setIdent(Aktoer ident) {
        this.ident = ident;
    }

    public void setTekst(String tekst) {
        this.tekst = tekst;
    }

    public Sikkerhetsavvik() {
    }

    public Sikkerhetsavvik(Aktoer ident, String tekst) {
        this.ident = ident;
        this.tekst = tekst;
    }

}
