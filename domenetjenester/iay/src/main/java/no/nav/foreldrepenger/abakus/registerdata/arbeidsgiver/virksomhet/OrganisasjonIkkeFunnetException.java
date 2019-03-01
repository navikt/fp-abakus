package no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;

public class OrganisasjonIkkeFunnetException extends IntegrasjonException {
    public OrganisasjonIkkeFunnetException(Feil feil) {
        super(feil);
    }
}
