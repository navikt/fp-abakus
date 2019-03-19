package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.inntekt;

import java.util.List;

import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.AktørDto;

public class InntekterDto {

    private AktørDto aktør;
    private List<UtbetalingDto> utbetalinger;

    public InntekterDto() {
    }

    public AktørDto getAktør() {
        return aktør;
    }

    public void setAktør(AktørDto aktør) {
        this.aktør = aktør;
    }

    public List<UtbetalingDto> getUtbetalinger() {
        return utbetalinger;
    }

    public void setUtbetalinger(List<UtbetalingDto> utbetalinger) {
        this.utbetalinger = utbetalinger;
    }
}
