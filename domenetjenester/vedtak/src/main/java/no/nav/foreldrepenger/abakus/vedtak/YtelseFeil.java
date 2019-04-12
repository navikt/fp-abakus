package no.nav.foreldrepenger.abakus.vedtak;

import java.io.IOException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface YtelseFeil extends DeklarerteFeil {

    YtelseFeil FACTORY = FeilFactory.create(YtelseFeil.class);

    @TekniskFeil(feilkode = "FP-328673",
        feilmelding = "Feil under parsing av vedtak. key={%s} payload={%s}",
        logLevel = LogLevel.WARN)
    Feil parsingFeil(String key, String payload, IOException e);
}
