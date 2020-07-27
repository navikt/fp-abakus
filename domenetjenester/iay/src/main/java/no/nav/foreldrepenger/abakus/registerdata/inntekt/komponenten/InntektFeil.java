package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface InntektFeil extends DeklarerteFeil {

    InntektFeil FACTORY = FeilFactory.create(InntektFeil.class);

    @IntegrasjonFeil(feilkode = "FP-535194", feilmelding = "Fikk følgende sikkerhetsavvik ved kall til inntektstjenesten: %s.", logLevel = LogLevel.ERROR)
    Feil fikkSikkerhetsavvikFraInntekt(String avvikene);

    @IntegrasjonFeil(feilkode = "FP-824246", feilmelding = "Feil ved kall til inntektstjenesten. Meld til #team_registre og #produksjonshendelser hvis dette skjer over lengre tidsperiode.", logLevel = LogLevel.ERROR)
    Feil feilVedKallTilInntekt(Exception e);

    @TekniskFeil(feilkode = "FP-711674", feilmelding = "Kunne ikke mappe svar fra Inntektskomponenten: virksomhet=%s, inntektType=%s", logLevel = LogLevel.ERROR)
    Feil kunneIkkeMappeResponse(String virksomhet, String inntektType);
}
