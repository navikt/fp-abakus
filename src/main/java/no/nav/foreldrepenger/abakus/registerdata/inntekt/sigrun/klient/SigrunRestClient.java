package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.time.Year;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.felles.integrasjon.rest.NavHeaders;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, endpointProperty = "sigrunpgi.rs.url",
    endpointDefault = "http://sigrun.team-inntekt/api/v1/pensjonsgivendeinntektforfolketrygden",
    scopesProperty = "sigrunpgi.scopes", scopesDefault = "api://prod-fss.team-inntekt.sigrun/.default")
public class SigrunRestClient {

    private static final Year FØRSTE_PGI = Year.of(2017);
    private static final Logger LOG = LoggerFactory.getLogger(SigrunRestClient.class);

    private final RestClient client;
    private final RestConfig restConfig;

    public SigrunRestClient() {
        this.client = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
    }

    //api/v1/pensjonsgivendeinntektforfolketrygden
    public Optional<PgiFolketrygdenResponse> hentPensjonsgivendeInntektForFolketrygden(String fnr, Year år) {
        if (år.isBefore(FØRSTE_PGI)) {
            return Optional.empty();
        }
        var requestBody = new PensjonsgivendeInntektForFolketrygdenRequest(fnr, år.toString());
        var request = RestRequest.newPOSTJson(requestBody, restConfig.endpoint(), restConfig);

        HttpResponse<String> response = client.sendReturnUnhandled(request);
        return handleResponse(response).map(r -> DefaultJsonMapper.fromJson(r, PgiFolketrygdenResponse.class));
    }

    // Håndtere konvensjon om 404 for tilfelle som ikke finnes hos SKE.
    private static Optional<String> handleResponse(HttpResponse<String> response) {
        int status = response.statusCode();
        if (status >= HttpURLConnection.HTTP_OK && status < HttpURLConnection.HTTP_MULT_CHOICE) {
            return Optional.ofNullable(response.body()).filter(b -> !b.isEmpty());
        } else if (status == HttpURLConnection.HTTP_FORBIDDEN) {
            throw new ManglerTilgangException("F-018815", "Mangler tilgang. Fikk http-kode 403 fra server");
        } else if (status == HttpURLConnection.HTTP_NOT_FOUND) {
            LOG.info("Sigrun PGI NOT FOUND");
            return Optional.empty();
        } else {
            if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
                LOG.info("Sigrun unauth");
            }
            throw new IntegrasjonException("F-016912", String.format("Server svarte med feilkode http-kode '%s' og response var '%s'", status, response.body()));
        }
    }
    public record PensjonsgivendeInntektForFolketrygdenRequest(
        String personident,
        String inntektsaar,
        String rettighetspakke) {

        public PensjonsgivendeInntektForFolketrygdenRequest(String personident, String inntektsaar) {
            this(personident, inntektsaar, "navForeldrepenger");
        }
    }
}
