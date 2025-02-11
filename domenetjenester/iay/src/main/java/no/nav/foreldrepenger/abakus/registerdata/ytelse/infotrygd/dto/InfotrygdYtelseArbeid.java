package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektPeriodeType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public class InfotrygdYtelseArbeid {

    private String orgnr;
    private BigDecimal inntekt;
    private InntektPeriodeType inntektperiode;
    private Boolean refusjon;
    private LocalDate refusjonTom;

    public InfotrygdYtelseArbeid(String orgnr, BigDecimal inntekt, InntektPeriodeType inntektperiode, Boolean refusjon, LocalDate refusjonTom) {
        this.orgnr = orgnr;
        this.inntekt = inntekt;
        this.inntektperiode = inntektperiode;
        this.refusjon = refusjon;
        this.refusjonTom = refusjonTom;
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

    public Optional<LocalDate> getRefusjonTom() {
        return Optional.ofNullable(refusjonTom);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InfotrygdYtelseArbeid that = (InfotrygdYtelseArbeid) o;
        return Objects.equals(orgnr, that.orgnr) && Objects.equals(inntekt, that.inntekt) && Objects.equals(inntektperiode, that.inntektperiode)
            && Objects.equals(refusjon, that.refusjon) && Objects.equals(refusjonTom, that.refusjonTom);

    }

    @Override
    public int hashCode() {
        return Objects.hash(orgnr, inntekt, inntektperiode, refusjon, refusjonTom);
    }

    @Override
    public String toString() {
        return "InfotrygdYtelseArbeid{" + "orgnr='" + getOrgnrString() + '\'' + ", inntekt=" + inntekt + ", inntektperiode=" + inntektperiode
            + ", refusjon=" + refusjon + ", refusjon=" + refusjonTom + '}';
    }

    private String getOrgnrString() {
        if (orgnr == null) {
            return null;
        }
        int length = orgnr.length();
        if (length <= 4) {
            return "*".repeat(length);
        }
        return "*".repeat(length - 4) + orgnr.substring(length - 4);
    }
}



