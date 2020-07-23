package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

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

    @TekniskFeil(feilkode = "FP-263743", feilmelding = "Feil ved opprettelse av request mot inntektstjenesten.", logLevel = LogLevel.ERROR)
    Feil feilVedOpprettelseAvInntektRequest(DatatypeConfigurationException e);

    @IntegrasjonFeil(feilkode = "FP-824246", feilmelding = "Feil ved kall til inntektstjenesten. Meld til #team_registre og #produksjonshendelser hvis dette skjer over lengre tidsperiode.", logLevel = LogLevel.ERROR)
    Feil feilVedKallTilInntekt(Exception e);

    @IntegrasjonFeil(feilkode = "FP-824246", feilmelding = "Inntektstjeneesten er nede, og kall dit er skrudd av for at prosesstasker ikke skal henge og låse fpsak. Når inntektstjeneten er oppe igjen, endre i unleash fpabakus.disable.kall.inntektskomponenten.", logLevel = LogLevel.ERROR)
    Feil skruddAvKallTilInntekt();

    @TekniskFeil(feilkode = "FP-722674", feilmelding = "Kunne ikke serialisere response fra Inntektskomponenten.", logLevel = LogLevel.ERROR)
    Feil kunneIkkeSerialisereResponse(JAXBException e);

    @TekniskFeil(feilkode = "FP-711674", feilmelding = "Kunne ikke mappe svar fra Inntektskomponenten: virksomhet=%s, inntektType=%s", logLevel = LogLevel.ERROR)
    Feil kunneIkkeMappeResponse(String virksomhet, String inntektType);
}
