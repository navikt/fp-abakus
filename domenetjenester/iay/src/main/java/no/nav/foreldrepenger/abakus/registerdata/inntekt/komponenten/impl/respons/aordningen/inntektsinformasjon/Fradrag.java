package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

public class Fradrag {
    private BigDecimal beloep;
    private String beskrivelse;
    private YearMonth fradragsperiode;
    private LocalDateTime leveringstidspunkt;
    private Aktoer inntektspliktig;
    private Aktoer utbetaler;
    private Aktoer fradragGjelder;

    public BigDecimal getBeloep() {
        return this.beloep;
    }

    public String getBeskrivelse() {
        return this.beskrivelse;
    }

    public YearMonth getFradragsperiode() {
        return this.fradragsperiode;
    }

    public LocalDateTime getLeveringstidspunkt() {
        return this.leveringstidspunkt;
    }

    public Aktoer getInntektspliktig() {
        return this.inntektspliktig;
    }

    public Aktoer getUtbetaler() {
        return this.utbetaler;
    }

    public Aktoer getFradragGjelder() {
        return this.fradragGjelder;
    }

    public void setBeloep(BigDecimal beloep) {
        this.beloep = beloep;
    }

    public void setBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
    }

    public void setFradragsperiode(YearMonth fradragsperiode) {
        this.fradragsperiode = fradragsperiode;
    }

    public void setLeveringstidspunkt(LocalDateTime leveringstidspunkt) {
        this.leveringstidspunkt = leveringstidspunkt;
    }

    public void setInntektspliktig(Aktoer inntektspliktig) {
        this.inntektspliktig = inntektspliktig;
    }

    public void setUtbetaler(Aktoer utbetaler) {
        this.utbetaler = utbetaler;
    }

    public void setFradragGjelder(Aktoer fradragGjelder) {
        this.fradragGjelder = fradragGjelder;
    }

    public Fradrag() {
    }

    public Fradrag(BigDecimal beloep, String beskrivelse, YearMonth fradragsperiode, LocalDateTime leveringstidspunkt, Aktoer inntektspliktig, Aktoer utbetaler, Aktoer fradragGjelder) {
        this.beloep = beloep;
        this.beskrivelse = beskrivelse;
        this.fradragsperiode = fradragsperiode;
        this.leveringstidspunkt = leveringstidspunkt;
        this.inntektspliktig = inntektspliktig;
        this.utbetaler = utbetaler;
        this.fradragGjelder = fradragGjelder;
    }
}
