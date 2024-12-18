package no.nav.foreldrepenger.abakus.registerdata.callback;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriBuilderException;
import no.nav.abakus.callback.registerdata.CallbackDto;
import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, application = FpApplication.FPSAK)
public class FpsakKlient {

    private static final Logger LOG = LoggerFactory.getLogger(FpsakKlient.class);
    protected static final String CALLBACK_PATH = "/api/registerdata/iay/callback/v2";

    private final RestClient restClient;
    private final RestConfig restConfig;
    private final URI endpointFpsak;

    public FpsakKlient() {
        this.restClient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        this.endpointFpsak = UriBuilder.fromUri(restConfig.endpoint()).path(CALLBACK_PATH).build();
    }

    public void sendCallback(CallbackDto callbackDto) {
        try {
            LOG.info("Sender callback til fpsak.");
            restClient.sendReturnOptional(RestRequest.newPOSTJson(callbackDto, endpointFpsak, restConfig), String.class);
            LOG.info("Callback sendt OK til fpsak.");
        } catch (UriBuilderException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Ugyldig callback url ved callback etter registerinnhenting.");
        }
    }

}
