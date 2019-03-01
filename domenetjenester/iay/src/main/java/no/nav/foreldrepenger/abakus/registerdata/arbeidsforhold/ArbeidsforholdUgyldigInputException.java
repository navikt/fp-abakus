package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;

public class ArbeidsforholdUgyldigInputException extends IntegrasjonException {
    public ArbeidsforholdUgyldigInputException(Feil feil) {
        super(feil);
    }
}
