package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.arbeidsforhold;

import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.ArbeidsforholdRefDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.ArbeidsgiverDto;

public class ArbeidsforholdReferanseDto {

    private ArbeidsgiverDto arbeidsgiver;
    private ArbeidsforholdRefDto arbeidsforholdReferanse;

    public ArbeidsforholdReferanseDto() {
    }

    public ArbeidsgiverDto getArbeidsgiver() {
        return arbeidsgiver;
    }

    public void setArbeidsgiver(ArbeidsgiverDto arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    public ArbeidsforholdRefDto getArbeidsforholdReferanse() {
        return arbeidsforholdReferanse;
    }

    public void setArbeidsforholdReferanse(ArbeidsforholdRefDto arbeidsforholdReferanse) {
        this.arbeidsforholdReferanse = arbeidsforholdReferanse;
    }
}
