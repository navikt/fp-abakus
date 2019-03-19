package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.inntekt;

import java.math.BigDecimal;

import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.PeriodeDto;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkDto;

public class UtbetalingsPostDto {

    private KodeverkDto type;
    private KodeverkDto skattAvgiftType;
    private PeriodeDto periode;
    private BigDecimal beløp;

    public UtbetalingsPostDto(KodeverkDto type, KodeverkDto skattAvgiftType, PeriodeDto periode, BigDecimal beløp) {
        this.type = type;
        this.skattAvgiftType = skattAvgiftType;
        this.periode = periode;
        this.beløp = beløp;
    }

    public UtbetalingsPostDto() {
    }

    public KodeverkDto getType() {
        return type;
    }

    public void setType(KodeverkDto type) {
        this.type = type;
    }

    public KodeverkDto getSkattAvgiftType() {
        return skattAvgiftType;
    }

    public void setSkattAvgiftType(KodeverkDto skattAvgiftType) {
        this.skattAvgiftType = skattAvgiftType;
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
}
