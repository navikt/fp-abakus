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
public class InnfotrygdRestSVPHealthCheck extends ExtHealthCheck {

    @Inject
    @KonfigVerdi(value = "fpabakus.it.svp.grunnlag.url")
    private String restUrl;  // NOSONAR

    InnfotrygdRestSVPHealthCheck() {
        // for CDI proxy
    }

    @Override
    protected String getDescription() {
        return "Test av rs Infotrygd Svangerskapspenger ";
    }

    @Override
    protected String getEndpoint() {
        return restUrl;
    }

    @Override
    protected InternalResult performCheck() {


        InternalResult intTestRes = new InternalResult();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(getEndpoint() + "/../../../../");
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
                int responseCode = httpResponse.getStatusLine().getStatusCode();
                if (responseCode != HttpStatus.SC_OK) { // Kaller med GET på et POST endepunkt. 405 validerer at forventet tjenste er der.
                    intTestRes.setMessage("Fikk uventet HTTP respons-kode: " + responseCode);
                    intTestRes.noteResponseTime();
                    return intTestRes;
                }
            }
        } catch (IOException e) {
            intTestRes.noteResponseTime();
            intTestRes.setException(e);
            return intTestRes;
        }

        intTestRes.noteResponseTime();
        intTestRes.setOk(true);
        return intTestRes;
    }
}
