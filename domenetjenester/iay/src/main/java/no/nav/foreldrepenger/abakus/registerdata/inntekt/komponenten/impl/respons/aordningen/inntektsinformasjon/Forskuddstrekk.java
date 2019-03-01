package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon;

import java.time.LocalDateTime;

public class Forskuddstrekk {
    private int beloep;
    private String beskrivelse;
    private LocalDateTime leveringstidspunkt;
    private Aktoer opplysningspliktig;
    private Aktoer utbetaler;
    private Aktoer forskuddstrekkGjelder;

    public int getBeloep() {
        return this.beloep;
    }

    public String getBeskrivelse() {
        return this.beskrivelse;
    }

    public LocalDateTime getLeveringstidspunkt() {
        return this.leveringstidspunkt;
    }

    public Aktoer getOpplysningspliktig() {
        return this.opplysningspliktig;
    }

    public Aktoer getUtbetaler() {
        return this.utbetaler;
    }

    public Aktoer getForskuddstrekkGjelder() {
        return this.forskuddstrekkGjelder;
    }

    public void setBeloep(int beloep) {
        this.beloep = beloep;
    }

    public void setBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
    }

    public void setLeveringstidspunkt(LocalDateTime leveringstidspunkt) {
        this.leveringstidspunkt = leveringstidspunkt;
    }

    public void setOpplysningspliktig(Aktoer opplysningspliktig) {
        this.opplysningspliktig = opplysningspliktig;
    }

    public void setUtbetaler(Aktoer utbetaler) {
        this.utbetaler = utbetaler;
    }

    public void setForskuddstrekkGjelder(Aktoer forskuddstrekkGjelder) {
        this.forskuddstrekkGjelder = forskuddstrekkGjelder;
    }

    public Forskuddstrekk() {
    }

    public Forskuddstrekk(int beloep, String beskrivelse, LocalDateTime leveringstidspunkt, Aktoer opplysningspliktig, Aktoer utbetaler, Aktoer forskuddstrekkGjelder) {
        this.beloep = beloep;
        this.beskrivelse = beskrivelse;
        this.leveringstidspunkt = leveringstidspunkt;
        this.opplysningspliktig = opplysningspliktig;
        this.utbetaler = utbetaler;
        this.forskuddstrekkGjelder = forskuddstrekkGjelder;
    }
}
