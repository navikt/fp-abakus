package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient;

import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestConfig.AUTH_HEADER;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestConfig.CALL_ID;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestConfig.CONSUMER_ID;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestConfig.FILTER;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestConfig.FILTER_SSG;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestConfig.INNTEKTSAAR;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestConfig.INNTEKTSFILTER;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestConfig.NAV_PERSONIDENT;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestConfig.NYE_HEADER_CALL_ID;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestConfig.NYE_HEADER_CONSUMER_ID;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestConfig.OIDC_AUTH_HEADER_PREFIX;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestConfig.PATH_SSG;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestConfig.X_AKTØRID;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestConfig.X_FILTER;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestConfig.X_INNTEKTSÅR;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.oidc.token.SikkerhetContext;
import no.nav.vedtak.sikkerhet.oidc.token.TokenProvider;

public class SigrunRestClient {
    private static final Logger LOG = LoggerFactory.getLogger(SigrunRestClient.class);
    private final ResponseHandler<String> defaultResponseHandler;
    private CloseableHttpClient client;
    private URI endpoint;

    SigrunRestClient(CloseableHttpClient closeableHttpClient) {
        super();
        client = closeableHttpClient;
        defaultResponseHandler = createDefaultResponseHandler();
    }

    //api/beregnetskatt
    String hentBeregnetSkattForAktørOgÅr(long aktørId, String år) {
        HttpRequestBase request = lagRequestBS(år, aktørId);
        String response;
        try {
            response = client.execute(request, defaultResponseHandler);
        } catch (IOException e) {
            throw ioException(e);
        } finally {
            request.reset();
        }
        return response;
    }

    //api/v1/summertskattegrunnlag
    String hentSummertskattegrunnlag(long aktørId, String år) {
        HttpRequestBase request = lagRequestSSG(aktørId, år);
        String response;
        try {
            response = client.execute(request, defaultResponseHandler);
        } catch (IOException e) {
            throw ioException(e);
        } finally {
            request.reset();
        }
        return response;
    }

    private HttpRequestBase lagRequestSSG(long aktørId, String år) {
        URIBuilder builder = new URIBuilder(endpoint.resolve(endpoint.getPath() + PATH_SSG));
        builder.setParameter(INNTEKTSAAR, år);
        builder.setParameter(INNTEKTSFILTER, FILTER_SSG);
        URI build = null;
        try {
            build = builder.build();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Kunne ikke lage URI for hente data fra Sigrun", e);
        }
        HttpRequestBase request = new HttpGet(build);
        request.addHeader(NAV_PERSONIDENT, String.valueOf(aktørId));
        String authHeaderValue = OIDC_AUTH_HEADER_PREFIX + getOIDCToken();
        request.setHeader(AUTH_HEADER, authHeaderValue);
        request.setHeader(CALL_ID, MDCOperations.getCallId());
        request.setHeader(NYE_HEADER_CALL_ID, MDCOperations.getCallId());
        request.setHeader("Accept", "application/json");
        request.setHeader(CONSUMER_ID, SubjectHandler.getSubjectHandler().getConsumerId());
        request.setHeader(NYE_HEADER_CONSUMER_ID, SubjectHandler.getSubjectHandler().getConsumerId());

        return request;
    }

    private HttpRequestBase lagRequestBS(String år, long aktørId) {
        HttpRequestBase request = new HttpGet(endpoint.resolve(endpoint.getPath() + SigrunRestConfig.PATH));
        String authHeaderValue = OIDC_AUTH_HEADER_PREFIX + getOIDCToken();
        request.setHeader(AUTH_HEADER, authHeaderValue);
        request.addHeader(X_FILTER, FILTER);
        request.addHeader(X_AKTØRID, String.valueOf(aktørId));
        request.addHeader(X_INNTEKTSÅR, år);
        request.setHeader(CALL_ID, MDCOperations.getCallId());
        request.setHeader(NYE_HEADER_CALL_ID, MDCOperations.getCallId());
        request.setHeader("Accept", "application/json");
        request.setHeader(CONSUMER_ID, SubjectHandler.getSubjectHandler().getConsumerId());
        request.setHeader(NYE_HEADER_CONSUMER_ID, SubjectHandler.getSubjectHandler().getConsumerId());
        return request;
    }

    private String getOIDCToken() {
        String oidcToken = SubjectHandler.getSubjectHandler().getInternSsoToken();
        if (oidcToken != null) {
            return oidcToken;
        }

        var samlToken = SubjectHandler.getSubjectHandler().getSamlToken();
        if (samlToken != null) {
            return TokenProvider.getTokenFor(SikkerhetContext.SYSTEM).token();
        }
        throw new TekniskException("F-017072", "Klarte ikke å fremskaffe et OIDC token");
    }

    private ResponseHandler<String> createDefaultResponseHandler() {
        return response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            } else if (status == HttpStatus.SC_FORBIDDEN) {
                throw new ManglerTilgangException("F-018815", "Mangler tilgang. Fikk http-kode 403 fra server");
            } else if (status == HttpStatus.SC_NOT_FOUND) {
                LOG.trace("Sigrun: {}", response.getStatusLine().getReasonPhrase());
                return null;
            } else {
                throw new IntegrasjonException("F-016912",
                    String.format("Server svarte med feilkode http-kode '%s' og response var '%s'", status, response.getStatusLine().getReasonPhrase()));
            }
        };
    }

    void setEndpoint(URI endpoint) {
        this.endpoint = endpoint;
    }

    private static TekniskException ioException(IOException cause) {
        return new TekniskException( "F-012937", "IOException ved kommunikasjon med server", cause);
    }
}
