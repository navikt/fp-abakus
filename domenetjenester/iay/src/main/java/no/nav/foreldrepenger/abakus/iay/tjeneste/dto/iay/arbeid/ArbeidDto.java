package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.arbeid;

import java.util.List;

import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.AktørDto;

public class ArbeidDto {

    private AktørDto aktør;
    private List<YrkesaktivitetDto> yrkesaktiviteter;

    public ArbeidDto() {
    }

    public AktørDto getAktør() {
        return aktør;
    }

    public void setAktør(AktørDto aktør) {
        this.aktør = aktør;
    }

    public List<YrkesaktivitetDto> getYrkesaktiviteter() {
        return yrkesaktiviteter;
    }

    public void setYrkesaktiviteter(List<YrkesaktivitetDto> yrkesaktiviteter) {
        this.yrkesaktiviteter = yrkesaktiviteter;
    }
}
