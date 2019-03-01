package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class ArbeidsInntektMaaned {
    private YearMonth aarMaaned;
    private List<Avvik> avvikListe = new ArrayList<>();
    private ArbeidsInntektInformasjon arbeidsInntektInformasjon;


    public YearMonth getAarMaaned() {
        return this.aarMaaned;
    }

    public List<Avvik> getAvvikListe() {
        return this.avvikListe;
    }

    public ArbeidsInntektInformasjon getArbeidsInntektInformasjon() {
        return this.arbeidsInntektInformasjon;
    }

    public void setAarMaaned(YearMonth aarMaaned) {
        this.aarMaaned = aarMaaned;
    }

    public void setAvvikListe(List<Avvik> avvikListe) {
        this.avvikListe = avvikListe;
    }

    public void setArbeidsInntektInformasjon(ArbeidsInntektInformasjon arbeidsInntektInformasjon) {
        this.arbeidsInntektInformasjon = arbeidsInntektInformasjon;
    }

    public ArbeidsInntektMaaned() {
    }

    public ArbeidsInntektMaaned(YearMonth aarMaaned, List<Avvik> avvikListe, ArbeidsInntektInformasjon arbeidsInntektInformasjon) {
        this.aarMaaned = aarMaaned;
        this.avvikListe = avvikListe;
        this.arbeidsInntektInformasjon = arbeidsInntektInformasjon;
    }

}
