package no.nav.foreldrepenger.abakus.domene.iay;

import java.math.BigInteger;
import java.util.Map;

public class Statistikk {
    private Long antallGrunnlag;
    private Map<BigInteger, BigInteger> histogram;

    public Statistikk(Long antallGrunnlag, Map<BigInteger, BigInteger> histogram) {
        this.antallGrunnlag = antallGrunnlag;
        this.histogram = histogram;
    }

    public Long getAntallGrunnlag() {
        return antallGrunnlag;
    }

    public Map<BigInteger, BigInteger> getHistogram() {
        return histogram;
    }
}
