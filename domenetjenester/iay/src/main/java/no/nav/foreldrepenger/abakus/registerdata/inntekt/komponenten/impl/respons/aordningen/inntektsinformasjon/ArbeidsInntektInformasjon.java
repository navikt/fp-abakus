package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon;

import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon.inntekt.Inntekt;

public class ArbeidsInntektInformasjon {
    private List<ArbeidsforholdFrilanser> arbeidsforholdListe = new ArrayList<>();
    private List<Inntekt> inntektListe = new ArrayList<>();
    private List<Forskuddstrekk> forskuddstrekkListe = new ArrayList<>();
    private List<Fradrag> fradragListe = new ArrayList<>();


    public ArbeidsInntektInformasjon() {
    }

    public ArbeidsInntektInformasjon(List<ArbeidsforholdFrilanser> arbeidsforholdListe, List<Inntekt> inntektListe, List<Forskuddstrekk> forskuddstrekkListe, List<Fradrag> fradragListe) {
        this.arbeidsforholdListe = arbeidsforholdListe;
        this.inntektListe = inntektListe;
        this.forskuddstrekkListe = forskuddstrekkListe;
        this.fradragListe = fradragListe;
    }

    public List<ArbeidsforholdFrilanser> getArbeidsforholdListe() {
        return this.arbeidsforholdListe;
    }

    public void setArbeidsforholdListe(List<ArbeidsforholdFrilanser> arbeidsforholdListe) {
        this.arbeidsforholdListe = arbeidsforholdListe;
    }

    public List<Inntekt> getInntektListe() {
        return this.inntektListe;
    }

    public void setInntektListe(List<Inntekt> inntektListe) {
        this.inntektListe = inntektListe;
    }

    public List<Forskuddstrekk> getForskuddstrekkListe() {
        return this.forskuddstrekkListe;
    }

    public void setForskuddstrekkListe(List<Forskuddstrekk> forskuddstrekkListe) {
        this.forskuddstrekkListe = forskuddstrekkListe;
    }

    public List<Fradrag> getFradragListe() {
        return this.fradragListe;
    }

    public void setFradragListe(List<Fradrag> fradragListe) {
        this.fradragListe = fradragListe;
    }

}
