package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient;

import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestConfig.PATH_BS;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestConfig.PATH_SSG;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestConfig.X_CALL_ID;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SSGResponse;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.felles.integrasjon.rest.NavHeaders;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@RestClientConfig(tokenConfig = TokenFlow.CONTEXT, endpointProperty = "SigrunRestBeregnetSkatt.url", endpointDefault = "https://sigrun.nais.adeo.no")
public class SigrunRestClient {
    private static final Logger LOG = LoggerFactory.getLogger(SigrunRestClient.class);
    private final RestClient client;
    private final RestConfig restConfig;
    private URI endpointBS;
    private URI endpointSSG;


    SigrunRestClient(RestClient client) {
        this.client = client;
        this.restConfig = RestConfig.forClient(SigrunRestClient.class);
        this.endpointBS = restConfig.endpoint().resolve(restConfig.endpoint().getPath() + PATH_BS);
        this.endpointSSG = restConfig.endpoint().resolve(restConfig.endpoint().getPath() + PATH_SSG);
    }

    List<BeregnetSkatt> hentBeregnetSkattForAktørOgÅr(long aktørId, String år) {
        var request = RestRequest.newGET(endpointBS, restConfig)
            .header(SigrunRestConfig.X_FILTER, SigrunRestConfig.FILTER)
            .header(SigrunRestConfig.X_AKTØRID, String.valueOf(aktørId))
            .header(SigrunRestConfig.X_INNTEKTSÅR, år)
            .otherCallId(X_CALL_ID)
            .otherCallId(SigrunRestConfig.NYE_HEADER_CALL_ID)
            .header(SigrunRestConfig.CONSUMER_ID, SubjectHandler.getSubjectHandler().getConsumerId())
            .header(SigrunRestConfig.NYE_HEADER_CONSUMER_ID, SubjectHandler.getSubjectHandler().getConsumerId());

        HttpResponse<String> response = client.sendReturnUnhandled(request);
        return handleResponse(response)
            .map(r -> Arrays.asList(DefaultJsonMapper.fromJson(r, BeregnetSkatt[].class)))
            .orElse(new ArrayList<>());
    }

    //api/v1/summertskattegrunnlag
    Optional<SSGResponse> hentSummertskattegrunnlag(long aktørId, String år) {
        var uri = UriBuilder.fromUri(endpointSSG)
            .queryParam(SigrunRestConfig.INNTEKTSAAR, år)
            .queryParam(SigrunRestConfig.INNTEKTSFILTER, SigrunRestConfig.FILTER_SSG)
            .build();

        var request = RestRequest.newGET(uri, restConfig)
            .header(NavHeaders.HEADER_NAV_PERSONIDENT, String.valueOf(aktørId))
            .otherCallId(X_CALL_ID)
            .otherCallId(SigrunRestConfig.NYE_HEADER_CALL_ID)
            .header(SigrunRestConfig.CONSUMER_ID, SubjectHandler.getSubjectHandler().getConsumerId())
            .header(SigrunRestConfig.NYE_HEADER_CONSUMER_ID, SubjectHandler.getSubjectHandler().getConsumerId());

        HttpResponse<String> response = client.sendReturnUnhandled(request);
        return handleResponse(response).map(r -> DefaultJsonMapper.fromJson(r, SSGResponse.class));
    }

    private static Optional<String> handleResponse(HttpResponse<String> response) {
        int status = response.statusCode();
        var body = response.body();
        if (status >= HttpURLConnection.HTTP_OK && status < HttpURLConnection.HTTP_MULT_CHOICE) {
            return body != null && !body.isEmpty() ? Optional.of(body) : Optional.empty();
        } else if (status == HttpURLConnection.HTTP_FORBIDDEN) {
            throw new ManglerTilgangException("F-018815", "Mangler tilgang. Fikk http-kode 403 fra server");
        } else if (status == HttpURLConnection.HTTP_NOT_FOUND) {
            LOG.trace("Sigrun: {}", body);
            return Optional.empty();
        } else {
            if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
                var challenge = response.headers().allValues("WWW-Authenticate");
                LOG.info("Sigrun unauth: {}", challenge);
            }
            throw new IntegrasjonException("F-016912",
                String.format("Server svarte med feilkode http-kode '%s' og response var '%s'", status, body));
        }
    }

}
