package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.ytelse;

import java.util.List;

import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.AktørDto;

public class YtelserDto {
    private AktørDto aktør;
    private List<YtelseDto> ytelser;

    public YtelserDto() {
    }

    public AktørDto getAktør() {
        return aktør;
    }

    public void setAktør(AktørDto aktør) {
        this.aktør = aktør;
    }

    public List<YtelseDto> getYtelser() {
        return ytelser;
    }

    public void setYtelser(List<YtelseDto> ytelser) {
        this.ytelser = ytelser;
    }
}
