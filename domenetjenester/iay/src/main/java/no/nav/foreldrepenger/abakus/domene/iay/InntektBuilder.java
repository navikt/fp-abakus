package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Optional;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsKilde;

public class InntektBuilder {
    private final boolean oppdaterer;
    private InntektEntitet inntektEntitet;

    private InntektBuilder(InntektEntitet inntektEntitet, boolean oppdaterer) {
        this.inntektEntitet = inntektEntitet;
        this.oppdaterer = oppdaterer;
    }

    static InntektBuilder ny() {
        return new InntektBuilder(new InntektEntitet(), false);
    }

    static InntektBuilder oppdatere(Inntekt oppdatere) {
        return new InntektBuilder((InntektEntitet) oppdatere, true);
    }

    public static InntektBuilder oppdatere(Optional<Inntekt> oppdatere) {
        return oppdatere.map(InntektBuilder::oppdatere).orElseGet(InntektBuilder::ny);
    }

    public InntektBuilder medInntektsKilde(InntektsKilde inntektsKilde) {
        this.inntektEntitet.setInntektsKilde(inntektsKilde);
        return this;
    }

    public InntektBuilder leggTilInntektspost(InntektspostBuilder inntektspost) {
        InntektspostEntitet inntektspostEntitet = (InntektspostEntitet) inntektspost.build();
        inntektEntitet.leggTilInntektspost(inntektspostEntitet);
        return this;
    }

    public InntektBuilder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        this.inntektEntitet.setArbeidsgiver(arbeidsgiver);
        return this;
    }

    public InntektspostBuilder getInntektspostBuilder() {
        return inntektEntitet.getInntektspostBuilder();
    }

    boolean getErOppdatering() {
        return this.oppdaterer;
    }

    public Inntekt build() {
        if (inntektEntitet.hasValues()) {
            return inntektEntitet;
        }
        throw new IllegalStateException();
    }
}