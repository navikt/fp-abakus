package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;

public class InfotrygdUgyldigInputException extends IntegrasjonException {
    public InfotrygdUgyldigInputException(Feil feil) {
        super(feil);
    }
}
