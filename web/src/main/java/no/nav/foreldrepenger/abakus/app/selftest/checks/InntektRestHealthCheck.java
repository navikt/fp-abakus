package no.nav.foreldrepenger.abakus.app.selftest.checks;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class InntektRestHealthCheck  {

    @Inject
    @KonfigVerdi("hentinntektlistebolk.url")
    private String restUrl;  // NOSONAR

    InntektRestHealthCheck() {
        // for CDI proxy
    }

    private String getEndpoint() {
        return restUrl;
    }

    public boolean isReady() {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(getEndpoint() + "/../../../../");
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
                int responseCode = httpResponse.getStatusLine().getStatusCode();
                if (responseCode != HttpStatus.SC_OK) { // Kaller med GET på et POST endepunkt. 405 validerer at forventet tjenste er der.
                    return false;
                }
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }
}
