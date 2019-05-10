package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.ytelse;

import java.util.List;

import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.PeriodeDto;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.Fagsystem;

public class YtelseDto {

    private Fagsystem fagsystem;
    private YtelseType type;
    private String saksnummer;
    private PeriodeDto periode;
    private YtelseStatus status;
    private List<Anvisning> anvisninger;
    private Grunnlag grunnlag;

    public YtelseDto() {
    }

    public List<Anvisning> getAnvisninger() {
        return anvisninger;
    }

    public void setAnvisninger(List<Anvisning> anvisninger) {
        this.anvisninger = anvisninger;
    }

    public Grunnlag getGrunnlag() {
        return grunnlag;
    }

    public void setGrunnlag(Grunnlag grunnlag) {
        this.grunnlag = grunnlag;
    }

    public Fagsystem getFagsystem() {
        return fagsystem;
    }

    public void setFagsystem(Fagsystem fagsystem) {
        this.fagsystem = fagsystem;
    }

    public YtelseType getType() {
        return type;
    }

    public void setType(YtelseType type) {
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

    public YtelseStatus getStatus() {
        return status;
    }

    public void setStatus(YtelseStatus status) {
        this.status = status;
    }
}
