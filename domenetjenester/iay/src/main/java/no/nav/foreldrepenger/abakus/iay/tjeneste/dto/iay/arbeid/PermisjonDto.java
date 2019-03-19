package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.arbeid;

import java.math.BigDecimal;

import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.PeriodeDto;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkDto;

public class PermisjonDto {

    private PeriodeDto periode;
    private KodeverkDto type;
    private BigDecimal prosentsats;

    public PermisjonDto() {
    }

    public PeriodeDto getPeriode() {
        return periode;
    }

    public void setPeriode(PeriodeDto periode) {
        this.periode = periode;
    }

    public KodeverkDto getType() {
        return type;
    }

    public void setType(KodeverkDto type) {
        this.type = type;
    }

    public BigDecimal getProsentsats() {
        return prosentsats;
    }

    public void setProsentsats(BigDecimal prosentsats) {
        this.prosentsats = prosentsats;
    }
}
