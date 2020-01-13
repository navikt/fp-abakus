package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest;

import java.math.BigDecimal;
import java.util.Objects;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektPeriodeType;

public class InfotrygdYtelseArbeid {

    private String orgnr;
    private BigDecimal inntekt;
    private InntektPeriodeType inntektperiode;
    private Boolean refusjon;

    public InfotrygdYtelseArbeid(String orgnr, BigDecimal inntekt, InntektPeriodeType inntektperiode, Boolean refusjon) {
        this.orgnr = orgnr;
        this.inntekt = inntekt;
        this.inntektperiode = inntektperiode;
        this.refusjon = refusjon;
    }

    public InfotrygdYtelseArbeid(String orgnr, int inntekt, InntektPeriodeType inntektperiode, Boolean refusjon) {
        this.orgnr = orgnr;
        this.inntekt = new BigDecimal(inntekt);
        this.inntektperiode = inntektperiode;
        this.refusjon = refusjon;
    }

    public String getOrgnr() {
        return orgnr;
    }

    public BigDecimal getInntekt() {
        return inntekt;
    }

    public InntektPeriodeType getInntektperiode() {
        return inntektperiode;
    }

    public Boolean getRefusjon() {
        return refusjon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InfotrygdYtelseArbeid that = (InfotrygdYtelseArbeid) o;
        return Objects.equals(orgnr, that.orgnr) &&
            Objects.equals(inntekt, that.inntekt) &&
            Objects.equals(inntektperiode, that.inntektperiode) &&
            Objects.equals(refusjon, that.refusjon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgnr, inntekt, inntektperiode, refusjon);
    }

    @Override
    public String toString() {
        return "InfotrygdYtelseArbeid{" +
            "orgnr='" + orgnr + '\'' +
            ", inntekt=" + inntekt +
            ", inntektperiode=" + inntektperiode +
            ", refusjon=" + refusjon +
            '}';
    }
}
