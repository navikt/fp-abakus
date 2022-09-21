package no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.rest;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

/*
 * Dokumentasjon https://confluence.adeo.no/display/FEL/EREG+-+Tjeneste+REST+ereg.api
 * Swagger https://modapp-q1.adeo.no/ereg/api/swagger-ui.html#/
 */

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.CONTEXT, endpointProperty = "organisasjon.rs.url", endpointDefault = "https://modapp.adeo.no/ereg/api/v1/organisasjon")
public class OrganisasjonRestKlient {

    public static final String HEADER_NAV_CALL_ID = "Nav-Call-Id";

    private RestClient restClient;
    private URI endpoint;

    public OrganisasjonRestKlient() {
    }

    @Inject
    public OrganisasjonRestKlient(RestClient restClient) {
        this.restClient = restClient ;
        this.endpoint = RestConfig.endpointFromAnnotation(OrganisasjonRestKlient.class);
    }

    public OrganisasjonEReg hentOrganisasjon(String orgnummer)  {
        var request = URI.create(endpoint.toString() + "/" + orgnummer);
        return restClient.send(RestRequest.newGET(request, OrganisasjonRestKlient.class), OrganisasjonEReg.class);
    }

    public JuridiskEnhetVirksomheter hentJurdiskEnhetVirksomheter(String orgnummer) {
        try {
            var request = UriBuilder.fromUri(endpoint).path(orgnummer)
                .queryParam("inkluderHierarki", "true")
                .queryParam("inkluderHistorikk", "true")
                .build();
            return restClient.send(RestRequest.newGET(request, OrganisasjonRestKlient.class), JuridiskEnhetVirksomheter.class);
        } catch (IllegalArgumentException|UriBuilderException e) {
            throw new IllegalArgumentException("Utviklerfeil syntax-exception for hentJurdiskEnhetVirksomheter");
        }
    }

}
