package no.nav.foreldrepenger.abakus.registerdata;

import static no.nav.vedtak.feil.LogLevel.INFO;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface Feilene extends DeklarerteFeil {
    Feilene FACTORY = FeilFactory.create(Feilene.class);

    @TekniskFeil(feilkode = "FP-074125", feilmelding = "Mangler Infotrygdsak for Infotrygdgrunnlag av type %s identdato %s", logLevel = INFO)
    Feil manglerInfotrygdSak(String type, String dato);
}