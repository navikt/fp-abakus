package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.ytelse;

import java.math.BigDecimal;

import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.PeriodeDto;

public class Anvisning {

    private PeriodeDto periode;
    private BigDecimal beløp;
    private BigDecimal dagsats;
    private BigDecimal utbetalingsgrad;

    public Anvisning() {
    }

    public PeriodeDto getPeriode() {
        return periode;
    }

    public void setPeriode(PeriodeDto periode) {
        this.periode = periode;
    }

    public BigDecimal getBeløp() {
        return beløp;
    }

    public void setBeløp(BigDecimal beløp) {
        this.beløp = beløp;
    }

    public BigDecimal getDagsats() {
        return dagsats;
    }

    public void setDagsats(BigDecimal dagsats) {
        this.dagsats = dagsats;
    }

    public BigDecimal getUtbetalingsgrad() {
        return utbetalingsgrad;
    }

    public void setUtbetalingsgrad(BigDecimal utbetalingsgrad) {
        this.utbetalingsgrad = utbetalingsgrad;
    }
}
