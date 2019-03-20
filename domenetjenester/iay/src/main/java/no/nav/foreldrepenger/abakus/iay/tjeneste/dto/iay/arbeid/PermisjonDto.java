package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.arbeid;

import java.math.BigDecimal;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.PermisjonsbeskrivelseType;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.PeriodeDto;

public class PermisjonDto {

    private PeriodeDto periode;
    private PermisjonsbeskrivelseType type;
    private BigDecimal prosentsats;

    public PermisjonDto() {
    }

    public PeriodeDto getPeriode() {
        return periode;
    }

    public void setPeriode(PeriodeDto periode) {
        this.periode = periode;
    }

    public PermisjonsbeskrivelseType getType() {
        return type;
    }

    public void setType(PermisjonsbeskrivelseType type) {
        this.type = type;
    }

    public BigDecimal getProsentsats() {
        return prosentsats;
    }

    public void setProsentsats(BigDecimal prosentsats) {
        this.prosentsats = prosentsats;
    }
}
