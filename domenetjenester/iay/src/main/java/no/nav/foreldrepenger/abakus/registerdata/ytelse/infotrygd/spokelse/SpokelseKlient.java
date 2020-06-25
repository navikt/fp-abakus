package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.spokelse;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.rest.OAuth2RestClient;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class SpokelseKlient {

    private static final String DEFAULT_URI = "http://spokelse.default/grunnlag";

    private static final Logger LOG = LoggerFactory.getLogger(SpokelseKlient.class);

    private URI uri;
    private String uriString;
    private OAuth2RestClient restClient;

    @Inject
    public SpokelseKlient(
        @KonfigVerdi(value = "SPOKELSE_GRUNNLAG_URL", defaultVerdi = DEFAULT_URI) URI uri,
        @KonfigVerdi(value = "SPOKELSE_GRUNNLAG_SCOPES") String scopesCsv,
        @KonfigVerdi(value = "AZURE_CLIENT_ID") String clientId,
        @KonfigVerdi(value = "AZURE_CLIENT_SECRET") String clientSecret,
        @KonfigVerdi(value = "AZURE_V2_TOKEN_ENDPOINT") URI tokenEndpoint,
        @KonfigVerdi(value = "AZURE_HTTP_PROXY", required = false) URI httpProxy) {
        this.restClient = OAuth2RestClient.builder()
            .clientId(clientId)
            .clientSecret(clientSecret)
            .scopes(scopesFraCsv(scopesCsv))
            .tokenEndpoint(tokenEndpoint)
            .tokenEndpointProxy(httpProxy)
            .build();
        this.uri = uri;
        this.uriString = uri.toString();
    }

    SpokelseKlient() {
        // CDI
    }

    public List<SykepengeVedtak> hentGrunnlag(String fnr) {
        try {
            var request = new URIBuilder(uri)
                    .addParameter("fodselsnummer", fnr)
                    .build();
            var grunnlag = restClient.get(request, SykepengeVedtak[].class);
            LOG.info("abakus spokelse REST {} fikk grunnlag {}", uriString, Arrays.toString(grunnlag));
            return Arrays.asList(grunnlag);
        } catch (Exception e) {
            LOG.info("abakus spokelse Feil ved oppslag mot {}, returnerer ingen grunnlag", uriString, e);
            return Collections.emptyList();
        }
    }

    private static Set<String> scopesFraCsv(String scopesCsv) {
        return Set.of(scopesCsv.replace(" ", "").split(","));
    }
}
