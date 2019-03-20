package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.ytelse;

import java.math.BigDecimal;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.ArbeidsgiverDto;

public class Fordeling {

    private ArbeidsgiverDto arbeidsgiver;
    private BigDecimal beløp;
    private InntektPeriodeType hyppighet;

    public Fordeling() {
    }

    public ArbeidsgiverDto getArbeidsgiver() {
        return arbeidsgiver;
    }

    public void setArbeidsgiver(ArbeidsgiverDto arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    public BigDecimal getBeløp() {
        return beløp;
    }

    public void setBeløp(BigDecimal beløp) {
        this.beløp = beløp;
    }

    public InntektPeriodeType getHyppighet() {
        return hyppighet;
    }

    public void setHyppighet(InntektPeriodeType hyppighet) {
        this.hyppighet = hyppighet;
    }
}
