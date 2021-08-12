package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.Header;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicHeader;

import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.log.mdc.MDCOperations;

/*
 * Dokumentasjon https://confluence.adeo.no/display/FEL/AAREG+-+Tjeneste+REST+aareg.api
 * Swagger https://modapp-q1.adeo.no/aareg-services/api/swagger-ui.html
 */

@ApplicationScoped
public class AaregRestKlient {

    private static final String ENDPOINT_KEY = "aareg.rs.url";
    private static final String DEFAULT_URI = "https://modapp.adeo.no/aareg-services/api/v1/arbeidstaker";

    private static final String HEADER_NAV_CALL_ID = "Nav-Call-Id";
    private static final String HEADER_NAV_PERSONIDENT = "Nav-Personident";
    private static final String HEADER_NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";

    private OidcRestClient oidcRestClient;
    private URI endpoint;

    public AaregRestKlient() {
    }

    @Inject
    public AaregRestKlient(OidcRestClient oidcRestClient,
                           @KonfigVerdi(value = ENDPOINT_KEY, defaultVerdi = DEFAULT_URI) URI endpoint) {
        this.oidcRestClient = oidcRestClient;
        this.endpoint = endpoint;
    }

    public List<ArbeidsforholdRS> finnArbeidsforholdForArbeidstaker(String ident, LocalDate qfom, LocalDate qtom) {
        try {
            var request = new URIBuilder(endpoint.toString() + "/" + "arbeidsforhold")
                    .addParameter("ansettelsesperiodeFom", String.valueOf(qfom))
                    .addParameter("ansettelsesperiodeTom", String.valueOf(qtom))
                    .addParameter("regelverk", "A_ORDNINGEN")
                    .addParameter("historikk", "true")
                    .addParameter("sporingsinformasjon", "false")
                    .build();
            ArbeidsforholdRS[] match = oidcRestClient.get(request, lagHeader(ident), Set.of(HEADER_NAV_CONSUMER_TOKEN), ArbeidsforholdRS[].class);
            return Arrays.asList(match);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Utviklerfeil syntax-exception for finnArbeidsforholdForArbeidstaker");
        }
    }

    public List<ArbeidsforholdRS> finnArbeidsforholdForFrilanser(String ident, LocalDate qfom, LocalDate qtom) {
        try {
            var request = new URIBuilder(endpoint.toString() + "/" + "arbeidsforhold")
                .addParameter("ansettelsesperiodeFom", String.valueOf(qfom))
                .addParameter("ansettelsesperiodeTom", String.valueOf(qtom))
                .addParameter("arbeidsforholdtype", "frilanserOppdragstakerHonorarPersonerMm")
                .addParameter("regelverk", "A_ORDNINGEN")
                .addParameter("historikk", "true")
                .addParameter("sporingsinformasjon", "false")
                .build();
            ArbeidsforholdRS[] match = oidcRestClient.get(request, lagHeader(ident), Set.of(HEADER_NAV_CONSUMER_TOKEN), ArbeidsforholdRS[].class);
            return Arrays.asList(match);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Utviklerfeil syntax-exception for finnArbeidsforholdForArbeidstaker");
        }
    }

    private Set<Header> lagHeader(String ident) {
        return Set.of(new BasicHeader(HEADER_NAV_CALL_ID, MDCOperations.getCallId()),
                new BasicHeader(HEADER_NAV_PERSONIDENT, ident));
    }
}
