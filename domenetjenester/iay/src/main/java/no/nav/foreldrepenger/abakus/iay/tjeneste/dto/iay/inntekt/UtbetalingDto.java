package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.inntekt;

import java.util.List;

import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.ArbeidsgiverDto;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkDto;

public class UtbetalingDto {

    private ArbeidsgiverDto utbetaler;
    private KodeverkDto kilde;
    private List<UtbetalingsPostDto> poster;

    public UtbetalingDto() {
    }

    public ArbeidsgiverDto getUtbetaler() {
        return utbetaler;
    }

    public void setUtbetaler(ArbeidsgiverDto utbetaler) {
        this.utbetaler = utbetaler;
    }

    public KodeverkDto getKilde() {
        return kilde;
    }

    public void setKilde(KodeverkDto kilde) {
        this.kilde = kilde;
    }

    public List<UtbetalingsPostDto> getPoster() {
        return poster;
    }

    public void setPoster(List<UtbetalingsPostDto> poster) {
        this.poster = poster;
    }
}
