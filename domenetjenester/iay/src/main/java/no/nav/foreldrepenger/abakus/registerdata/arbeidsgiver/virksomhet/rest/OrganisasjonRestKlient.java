package no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.rest;

import java.net.URI;
import java.net.URISyntaxException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.client.utils.URIBuilder;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

/*
 * Dokumentasjon https://confluence.adeo.no/display/FEL/EREG+-+Tjeneste+REST+ereg.api
 * Swagger https://modapp-q1.adeo.no/ereg/api/swagger-ui.html#/
 */

@ApplicationScoped
public class OrganisasjonRestKlient {

    private static final String ENDPOINT_KEY = "organisasjon.rs.url";
    private static final String DEFAULT_URI = "https://modapp.adeo.no/ereg/api/v1/organisasjon";

    public static final String HEADER_NAV_CALL_ID = "Nav-Call-Id";

    private OidcRestClient oidcRestClient;
    private URI endpoint;

    public OrganisasjonRestKlient() {
    }

    @Inject
    public OrganisasjonRestKlient(OidcRestClient oidcRestClient,
                                  @KonfigVerdi(value = ENDPOINT_KEY, defaultVerdi = DEFAULT_URI) URI endpoint) {
        this.oidcRestClient = oidcRestClient ;
        this.endpoint = endpoint;
    }

    public OrganisasjonEReg hentOrganisasjon(String orgnummer)  {
        var request = URI.create(endpoint.toString() + "/" + orgnummer);
        return oidcRestClient.get(request, OrganisasjonEReg.class);
    }

    public JuridiskEnhetVirksomheter hentJurdiskEnhetVirksomheter(String orgnummer) {
        try {
            var request = new URIBuilder(endpoint.toString() + "/" + orgnummer)
                .addParameter("inkluderHierarki", "true")
                .addParameter("inkluderHistorikk", "true")
                .build();
            return oidcRestClient.get(request, JuridiskEnhetVirksomheter.class);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Utviklerfeil syntax-exception for hentJurdiskEnhetVirksomheter");
        }
    }

}
