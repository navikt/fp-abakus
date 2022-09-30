package no.nav.foreldrepenger.abakus.registerdata;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.STS_CC, endpointProperty = "fpwsproxy.rs.url", endpointDefault = "https://fp-ws-proxy/api/arena")
public class FpwsproxyKlient {
    private static final Logger LOG = LoggerFactory.getLogger(FpwsproxyKlient.class);
    private RestClient restClient;
    private URI endpoint;

    public FpwsproxyKlient() {
    }

    @Inject
    public FpwsproxyKlient(RestClient restClient) {
        this.restClient = restClient;
        this.endpoint = RestConfig.endpointFromAnnotation(FpwsproxyKlient.class);
    }

    public List<MeldekortUtbetalingsgrunnlagSak> hentDagpengerAAP(PersonIdent ident, LocalDate fom, LocalDate tom) {
        try {
            LOG.info("Henter dagpengerAAP for {} i periode fom {} tom {}", ident.getIdent(), fom, tom);
            var target = UriBuilder.fromUri(endpoint).build();
            var body = new ArenaRequestDto(ident.getIdent(), fom, tom);
            var request = RestRequest.newPOSTJson(body, target, MeldekortUtbetalingsgrunnlagSak[].class);
            LOG.info("Sender request til fp-ws-proxy arena {}", request);
            var result = restClient.send(request, MeldekortUtbetalingsgrunnlagSak[].class);
            LOG.info("Resultat mottatt er {} ", result);
            return Arrays.asList(result);
        } catch (UriBuilderException|IllegalArgumentException e) {
            throw new IllegalArgumentException("Utviklerfeil syntax-exception for hentDagpengerAAP");
        }
    }
}
