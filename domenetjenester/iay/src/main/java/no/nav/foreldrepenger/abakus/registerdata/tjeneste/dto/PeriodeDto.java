package no.nav.foreldrepenger.abakus.registerdata.tjeneste.dto;

import java.time.LocalDate;

public class PeriodeDto {

    private LocalDate fom;
    private LocalDate tom;

    public PeriodeDto() {
    }

    public PeriodeDto(LocalDate fom, LocalDate tom) {
        this.fom = fom;
        this.tom = tom;
    }

    public LocalDate getFom() {
        return fom;
    }

    public void setFom(LocalDate fom) {
        this.fom = fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }
}
