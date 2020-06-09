package no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;


public interface OrganisasjonTjenesteFeil extends DeklarerteFeil {

    OrganisasjonTjenesteFeil FACTORY = FeilFactory.create(OrganisasjonTjenesteFeil.class);

    @TekniskFeil(feilkode = "FP-36379", feilmelding = "Organisasjon er Orgledd", logLevel = LogLevel.ERROR)
    Feil organisasjonErOrgledd(String tjeneste);
}
