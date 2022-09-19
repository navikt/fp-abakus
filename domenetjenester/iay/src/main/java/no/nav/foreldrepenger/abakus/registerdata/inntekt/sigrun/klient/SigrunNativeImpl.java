package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient;

import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestNativeConfig.CONSUMER_ID;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestNativeConfig.FILTER;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestNativeConfig.FILTER_SSG;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestNativeConfig.INNTEKTSAAR;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestNativeConfig.INNTEKTSFILTER;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestNativeConfig.NYE_HEADER_CALL_ID;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestNativeConfig.NYE_HEADER_CONSUMER_ID;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestNativeConfig.PATH_BS;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestNativeConfig.PATH_SSG;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestNativeConfig.X_AKTØRID;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestNativeConfig.X_CALL_ID;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestNativeConfig.X_FILTER;
import static no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestNativeConfig.X_INNTEKTSÅR;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpResponse;
import java.time.MonthDay;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SSGResponse;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SigrunSummertSkattegrunnlagResponse;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.felles.integrasjon.rest.NativeClient;
import no.nav.vedtak.felles.integrasjon.rest.NavHeaders;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@NativeClient
@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.CONTEXT, endpointProperty = "SigrunRestBeregnetSkatt.url", endpointDefault = "https://sigrun.nais.adeo.no")
public class SigrunNativeImpl implements SigrunConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(SigrunNativeImpl.class);
    private static final String TEKNISK_NAVN = "skatteoppgjoersdato";

    private static final MonthDay TIDLIGSTE_SJEKK_FJOR = MonthDay.of(5, 1);

    private RestClient client;
    private URI endpointBS;
    private URI endpointSSG;
    private boolean isProd = Environment.current().isProd();


    SigrunNativeImpl() {
        //CDI
    }

    @Inject
    public SigrunNativeImpl(RestClient restClient) {
        this.client = restClient;
        var endpoint = RestConfig.endpointFromAnnotation(SigrunNativeImpl.class);
        this.endpointBS = endpoint.resolve(endpoint.getPath() + PATH_BS);
        this.endpointSSG = endpoint.resolve(endpoint.getPath() + PATH_SSG);
    }

    @Override
    public SigrunResponse beregnetskatt(Long aktørId) {
         var årTilListeMedSkatt = ferdiglignedeBeregnetSkattÅr(aktørId).stream()
            .collect(Collectors.toMap(år -> år, år -> hentBeregnetSkattForAktørOgÅr(aktørId, år.toString())));

        return new SigrunResponse(årTilListeMedSkatt);
    }

    @Override
    public SigrunSummertSkattegrunnlagResponse summertSkattegrunnlag(Long aktørId) {
        Map<Year, Optional<SSGResponse>> summertskattegrunnlagMap = hentÅrsListeForSummertskattegrunnlag(aktørId).stream()
            .collect(Collectors.toMap(år -> år, år -> hentSummertskattegrunnlag(aktørId, år.toString())));
        return new SigrunSummertSkattegrunnlagResponse(summertskattegrunnlagMap);
    }

    private List<Year> ferdiglignedeBeregnetSkattÅr(Long aktørId) {
        Year iFjor = Year.now().minusYears(1L);
        if (iFjorErFerdiglignetBeregnet(aktørId, iFjor)) {
            return asList(iFjor, iFjor.minusYears(1L), iFjor.minusYears(2L));
        } else {
            Year iForifjor = iFjor.minusYears(1L);
            return asList(iForifjor, iForifjor.minusYears(1L), iForifjor.minusYears(2L));
        }
    }

    private List<Year> hentÅrsListeForSummertskattegrunnlag(Long aktørId) {
        Year iFjor = Year.now().minusYears(1L);
        //filteret(SummertSkattegrunnlagForeldrepenger) i Sigrun er ikke impl. tidligere enn 2018
        if (iFjor.equals(Year.of(2018))) {
            return List.of(iFjor);
        } else if (iFjor.equals(Year.of(2019))) {
            return List.of(iFjor, iFjor.minusYears(1L));
        }
        return ferdiglignedeBeregnetSkattÅr(aktørId);
    }

    private boolean iFjorErFerdiglignetBeregnet(Long aktørId, Year iFjor) {
        if (isProd && Year.now().minusYears(1).equals(iFjor) && MonthDay.now().isBefore(TIDLIGSTE_SJEKK_FJOR)) return false;
        return hentBeregnetSkattForAktørOgÅr(aktørId, iFjor.toString()).stream()
            .anyMatch(l -> l.tekniskNavn().equals(TEKNISK_NAVN));
    }

    List<BeregnetSkatt> hentBeregnetSkattForAktørOgÅr(long aktørId, String år) {
        var request = RestRequest.newGET(endpointBS, SigrunNativeImpl.class)
            .header(X_FILTER, FILTER)
            .header(X_AKTØRID, String.valueOf(aktørId))
            .header(X_INNTEKTSÅR, år)
            .otherCallId(X_CALL_ID)
            .otherCallId(NYE_HEADER_CALL_ID)
            .header(CONSUMER_ID, SubjectHandler.getSubjectHandler().getConsumerId())
            .header(NYE_HEADER_CONSUMER_ID, SubjectHandler.getSubjectHandler().getConsumerId());

        HttpResponse<String> response = client.sendReturnUnhandled(request);
        return handleResponse(response)
            .map(r -> Arrays.asList(DefaultJsonMapper.fromJson(r, BeregnetSkatt[].class)))
            .orElse(new ArrayList<>());
    }

    //api/v1/summertskattegrunnlag
    Optional<SSGResponse> hentSummertskattegrunnlag(long aktørId, String år) {
        var uri = UriBuilder.fromUri(endpointSSG)
            .queryParam(INNTEKTSAAR, år)
            .queryParam(INNTEKTSFILTER, FILTER_SSG)
            .build();

        var request = RestRequest.newGET(uri, SigrunNativeImpl.class)
            .header(NavHeaders.HEADER_NAV_PERSONIDENT, String.valueOf(aktørId))
            .otherCallId(X_CALL_ID)
            .otherCallId(NYE_HEADER_CALL_ID)
            .header(CONSUMER_ID, SubjectHandler.getSubjectHandler().getConsumerId())
            .header(NYE_HEADER_CONSUMER_ID, SubjectHandler.getSubjectHandler().getConsumerId());

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
