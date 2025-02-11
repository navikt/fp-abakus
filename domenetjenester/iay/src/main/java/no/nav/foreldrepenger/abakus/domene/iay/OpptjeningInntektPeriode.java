package no.nav.foreldrepenger.abakus.domene.iay;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OpptjeningInntektPeriode {

    private IntervallEntitet periode;
    private BigDecimal beløp;
    private Opptjeningsnøkkel opptjeningsnøkkel;
    private InntektspostType type;

    public OpptjeningInntektPeriode(Inntektspost inntektspost, Opptjeningsnøkkel opptjeningsnøkkel) {
        this.periode = inntektspost.getPeriode();
        this.beløp = inntektspost.getBeløp().getVerdi();
        this.opptjeningsnøkkel = opptjeningsnøkkel;
        this.type = inntektspost.getInntektspostType();
    }

    public LocalDate getFraOgMed() {
        return periode.getFomDato();
    }

    public LocalDate getTilOgMed() {
        return periode.getTomDato();
    }

    public BigDecimal getBeløp() {
        return beløp;
    }

    public Opptjeningsnøkkel getOpptjeningsnøkkel() {
        return opptjeningsnøkkel;
    }

    public InntektspostType getType() {
        return type;
    }
}
