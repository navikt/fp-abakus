package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon;

import java.time.YearMonth;

public class Avvik {
    private Aktoer ident;
    private Aktoer opplysningspliktig;
    private Aktoer virksomhet;
    private YearMonth avvikPeriode;
    private String tekst;

    public Aktoer getIdent() {
        return this.ident;
    }

    public Aktoer getOpplysningspliktig() {
        return this.opplysningspliktig;
    }

    public Aktoer getVirksomhet() {
        return this.virksomhet;
    }

    public YearMonth getAvvikPeriode() {
        return this.avvikPeriode;
    }

    public String getTekst() {
        return this.tekst;
    }

    public void setIdent(Aktoer ident) {
        this.ident = ident;
    }

    public void setOpplysningspliktig(Aktoer opplysningspliktig) {
        this.opplysningspliktig = opplysningspliktig;
    }

    public void setVirksomhet(Aktoer virksomhet) {
        this.virksomhet = virksomhet;
    }

    public void setAvvikPeriode(YearMonth avvikPeriode) {
        this.avvikPeriode = avvikPeriode;
    }

    public void setTekst(String tekst) {
        this.tekst = tekst;
    }

    public Avvik() {
    }

    public Avvik(Aktoer ident, Aktoer opplysningspliktig, Aktoer virksomhet, YearMonth avvikPeriode, String tekst) {
        this.ident = ident;
        this.opplysningspliktig = opplysningspliktig;
        this.virksomhet = virksomhet;
        this.avvikPeriode = avvikPeriode;
        this.tekst = tekst;
    }
}
