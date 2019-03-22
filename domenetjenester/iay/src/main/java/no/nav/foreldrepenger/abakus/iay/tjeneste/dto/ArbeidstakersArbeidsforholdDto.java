package no.nav.foreldrepenger.abakus.iay.tjeneste.dto;

import java.util.List;

import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.arbeidsforhold.ArbeidsforholdDto;

public class ArbeidstakersArbeidsforholdDto {
    private List<ArbeidsforholdDto> arbeidsforhold;

    public ArbeidstakersArbeidsforholdDto() {
    }

    public List<ArbeidsforholdDto> getArbeidsforhold() {
        return arbeidsforhold;
    }

    public void setArbeidsforhold(List<ArbeidsforholdDto> arbeidsforhold) {
        this.arbeidsforhold = arbeidsforhold;
    }
}
