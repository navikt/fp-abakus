package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;

public class InfotrygdPersonIkkeFunnetException extends IntegrasjonException {
    public InfotrygdPersonIkkeFunnetException(Feil feil) {
        super(feil);
    }
}
