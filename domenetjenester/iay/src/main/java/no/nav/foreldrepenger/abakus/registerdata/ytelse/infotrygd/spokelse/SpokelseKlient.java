package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.spokelse;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class SpokelseKlient {

    private static final String DEFAULT_URI = "http://spokelse.default/grunnlag";

    private static final Logger LOG = LoggerFactory.getLogger(SpokelseKlient.class);

    private OidcRestClient restClient;
    private URI uri;
    private String uriString;

    @Inject
    public SpokelseKlient(OidcRestClient restClient, @KonfigVerdi(value = "spokelse.grunnlag.url", defaultVerdi = DEFAULT_URI) URI uri) {
        this.restClient = restClient;
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
}
