package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.felles.integrasjon.rest.*;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.time.Year;
import java.util.Optional;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, endpointProperty = "sigrunpgi.rs.url",
    endpointDefault = "http://sigrun.team-inntekt/api/v1/pensjonsgivendeinntektforfolketrygden",
    scopesProperty = "sigrunpgi.scopes", scopesDefault = "api://prod-fss.team-inntekt.sigrun/.default")
public class SigrunRestClient {

    private static final String INNTEKTSAAR = "inntektsaar";
    private static final String RETTIGHETSPAKKE = "rettighetspakke";
    private static final String FORELDREPENGER = "navForeldrepenger";

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
        var request = RestRequest.newGET(restConfig.endpoint(), restConfig)
            .header(NavHeaders.HEADER_NAV_PERSONIDENT, fnr)
            .header(RETTIGHETSPAKKE, FORELDREPENGER)
            .header(INNTEKTSAAR, år.toString());

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

}
