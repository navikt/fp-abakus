package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import no.nav.vedtak.felles.integrasjon.rest.NavHeaders;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

/*
 * Dokumentasjon https://confluence.adeo.no/display/FEL/AAREG+-+Tjeneste+REST+aareg.api
 * Swagger https://modapp-q1.adeo.no/aareg-services/api/swagger-ui.html
 */

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.CONTEXT_ADD_CONSUMER, endpointProperty = "aareg.rs.url", endpointDefault = "https://modapp.adeo.no/aareg-services/api/v1/arbeidstaker")
public class AaregRestKlient {


    private RestClient restClient; // Setter på consumer-token fra STS
    private URI endpoint;

    public AaregRestKlient() {
    }

    @Inject
    public AaregRestKlient(RestClient restClient) {
        this.restClient = restClient;
        this.endpoint = RestConfig.endpointFromAnnotation(AaregRestKlient.class);
    }

    public List<ArbeidsforholdRS> finnArbeidsforholdForArbeidstaker(String ident, LocalDate qfom, LocalDate qtom) {
        try {
            var target = UriBuilder.fromUri(endpoint).path("arbeidsforhold")
                    .queryParam("ansettelsesperiodeFom", String.valueOf(qfom))
                    .queryParam("ansettelsesperiodeTom", String.valueOf(qtom))
                    .queryParam("regelverk", "A_ORDNINGEN")
                    .queryParam("historikk", "true")
                    .queryParam("sporingsinformasjon", "false")
                    .build();
            var request = RestRequest.newGET(target, AaregRestKlient.class)
                .header(NavHeaders.HEADER_NAV_PERSONIDENT, ident);
            var result = restClient.send(request, ArbeidsforholdRS[].class);
            return Arrays.asList(result);
        } catch (UriBuilderException|IllegalArgumentException e) {
            throw new IllegalArgumentException("Utviklerfeil syntax-exception for finnArbeidsforholdForArbeidstaker");
        }
    }

    public List<ArbeidsforholdRS> finnArbeidsforholdForFrilanser(String ident, LocalDate qfom, LocalDate qtom) {
        try {
            var target = UriBuilder.fromUri(endpoint).path("arbeidsforhold")
                .queryParam("ansettelsesperiodeFom", String.valueOf(qfom))
                .queryParam("ansettelsesperiodeTom", String.valueOf(qtom))
                .queryParam("arbeidsforholdtype", "frilanserOppdragstakerHonorarPersonerMm")
                .queryParam("regelverk", "A_ORDNINGEN")
                .queryParam("historikk", "true")
                .queryParam("sporingsinformasjon", "false")
                .build();
            var request = RestRequest.newGET(target, AaregRestKlient.class)
                .header(NavHeaders.HEADER_NAV_PERSONIDENT, ident);
            var result = restClient.send(request, ArbeidsforholdRS[].class);
            return Arrays.asList(result);
        } catch (UriBuilderException|IllegalArgumentException e) {
            throw new IllegalArgumentException("Utviklerfeil syntax-exception for finnArbeidsforholdForArbeidstaker");
        }
    }
}
