package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.inntekt;

import java.math.BigDecimal;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.SkatteOgAvgiftsregelType;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.PeriodeDto;

public class UtbetalingsPostDto {

    private InntektspostType type;
    private SkatteOgAvgiftsregelType skattAvgiftType;
    private PeriodeDto periode;
    private BigDecimal beløp;

    public UtbetalingsPostDto() {
    }

    public InntektspostType getType() {
        return type;
    }

    public void setType(InntektspostType type) {
        this.type = type;
    }

    public SkatteOgAvgiftsregelType getSkattAvgiftType() {
        return skattAvgiftType;
    }

    public void setSkattAvgiftType(SkatteOgAvgiftsregelType skattAvgiftType) {
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
