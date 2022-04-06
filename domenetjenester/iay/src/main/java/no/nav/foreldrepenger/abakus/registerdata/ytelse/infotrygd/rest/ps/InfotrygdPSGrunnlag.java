package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.ps;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.util.EntityUtils;

import no.nav.abakus.iaygrunnlag.JsonObjectMapper;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.felles.AbstractInfotrygdGrunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.felles.JsonConverter;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.felles.PersonRequest;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Grunnlag;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

@ApplicationScoped
@PS
public class InfotrygdPSGrunnlag extends AbstractInfotrygdGrunnlag {

    private static final String DEFAULT_URI = "http://infotrygd-grunnlag-paaroerende-sykdom.default/paaroerendeSykdom/grunnlag";
    private final JsonConverter jsonConverter = new JsonConverter();

    @Inject
    public InfotrygdPSGrunnlag(OidcRestClient restClient, @KonfigVerdi(value = "fpabakus.it.ps.grunnlag.url", defaultVerdi = DEFAULT_URI) URI uri) {
        super(restClient, uri);
    }

    public InfotrygdPSGrunnlag() {
        super();
    }


    @Override
    public List<Grunnlag> hentGrunnlag(String fnr, LocalDate fom, LocalDate tom) {

        var jsonResponse = hentGrunnlagIJsonFormat(fnr, fom, tom);
        return jsonConverter.grunnlagBarnResponse(jsonResponse);

    }

    private String hentGrunnlagIJsonFormat(String fnr, LocalDate fom, LocalDate tom) {
        try {
            URIBuilder builder = new URIBuilder(uri);

            var request = new PersonRequest(fom, tom, List.of(fnr));
            var json = JsonObjectMapper.getJson(request);
            HttpPost httpPost = new HttpPost(builder.build());
            httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
            httpPost.addHeader(HttpHeaders.ACCEPT, "application/json");
            CloseableHttpResponse response;
            try {
                response = restClient.execute(httpPost);
                var statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() != 200) {
                    String feilmelding = String.format("Uventet statuskode %s for call mot %s. Feilmelding: %s", statusLine.getStatusCode(),
                        uriString, response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : null);
                    LOG.error(feilmelding);
                    throw new IllegalStateException(feilmelding);
                }
                return new BasicResponseHandler().handleResponse(response);
            } catch (IOException e) {
                LOG.error("Feil ved oppkobling mot API.", e);
                throw new IllegalStateException("Feil ved oppkobling mot API.", e);
            }
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException(String.format("Kunne ikke mappe til request for %s. Feilmelding var %s", uri, e.getMessage()));
        }
    }

}
