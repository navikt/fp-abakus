package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.ytelse;

import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.PeriodeDto;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkDto;

public class YtelseDto {

    private KodeverkDto fagsystem;
    private KodeverkDto type;
    private String saksnummer;
    private PeriodeDto periode;
    private KodeverkDto status;

    public YtelseDto(KodeverkDto fagsystem, KodeverkDto type, String saksnummer, PeriodeDto periode, KodeverkDto status) {
        this.fagsystem = fagsystem;
        this.type = type;
        this.saksnummer = saksnummer;
        this.periode = periode;
        this.status = status;
    }

    public YtelseDto() {
    }

    public KodeverkDto getFagsystem() {
        return fagsystem;
    }

    public void setFagsystem(KodeverkDto fagsystem) {
        this.fagsystem = fagsystem;
    }

    public KodeverkDto getType() {
        return type;
    }

    public void setType(KodeverkDto type) {
        this.type = type;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public PeriodeDto getPeriode() {
        return periode;
    }

    public void setPeriode(PeriodeDto periode) {
        this.periode = periode;
    }

    public KodeverkDto getStatus() {
        return status;
    }

    public void setStatus(KodeverkDto status) {
        this.status = status;
    }
}
