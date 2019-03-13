package no.nav.foreldrepenger.abakus.kobling.repository;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface KoblingRepositoryFeil extends DeklarerteFeil {
    KoblingRepositoryFeil FACTORY = FeilFactory.create(KoblingRepositoryFeil.class);

    @TekniskFeil(feilkode = "FP-131239", feilmelding = "Fant ikke entitet for låsing [%s], id=%s.", logLevel = LogLevel.ERROR)
    Feil fantIkkeEntitetForLåsing(String entityClassName, long id);

}
