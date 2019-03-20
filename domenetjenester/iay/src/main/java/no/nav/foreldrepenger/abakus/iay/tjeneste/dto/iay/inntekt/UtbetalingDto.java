package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.inntekt;

import java.util.List;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.ArbeidsgiverDto;

public class UtbetalingDto {

    private ArbeidsgiverDto utbetaler;
    private InntektsKilde kilde;
    private List<UtbetalingsPostDto> poster;

    public UtbetalingDto() {
    }

    public ArbeidsgiverDto getUtbetaler() {
        return utbetaler;
    }

    public void setUtbetaler(ArbeidsgiverDto utbetaler) {
        this.utbetaler = utbetaler;
    }

    public InntektsKilde getKilde() {
        return kilde;
    }

    public void setKilde(InntektsKilde kilde) {
        this.kilde = kilde;
    }

    public List<UtbetalingsPostDto> getPoster() {
        return poster;
    }

    public void setPoster(List<UtbetalingsPostDto> poster) {
        this.poster = poster;
    }
}
