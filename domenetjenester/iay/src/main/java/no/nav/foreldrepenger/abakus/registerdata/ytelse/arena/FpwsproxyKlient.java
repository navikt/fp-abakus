package no.nav.foreldrepenger.abakus.registerdata.ytelse.arena;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.arena.request.ArenaRequestDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.arena.respons.MeldekortUtbetalingsgrunnlagSakDto;
import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.STS_CC, application = FpApplication.FPWSPROXY)
public class FpwsproxyKlient {

    private static final Logger LOG = LoggerFactory.getLogger(FpwsproxyKlient.class);

    private final RestClient restClient;
    private final RestConfig restConfig;
    private final URI endpointHentDagpengerAAP;

    public FpwsproxyKlient() {
        this.restClient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        this.endpointHentDagpengerAAP = UriBuilder.fromUri(restConfig.endpoint()).path("/arena").build();
    }

    public List<MeldekortUtbetalingsgrunnlagSak> hentDagpengerAAP(PersonIdent ident, LocalDate fom, LocalDate tom) {
        try {
            LOG.info("Henter dagpenger/AAP for {} i periode fom {} tom {}", ident, fom, tom);
            var body = new ArenaRequestDto(ident.getIdent(), fom, tom);
            var request = RestRequest.newPOSTJson(body, endpointHentDagpengerAAP, restConfig);
            var result = restClient.send(request, MeldekortUtbetalingsgrunnlagSakDto[].class);
            LOG.info("Dagpenger/AAP hentet OK");
            return Arrays.stream(result)
                .map(MedlemskortUtbetalingsgrunnlagSakMapper::tilDomeneModell)
                .toList();
        } catch (UriBuilderException|IllegalArgumentException e) {
            throw new IllegalArgumentException("Utviklerfeil syntax-exception for hentDagpengerAAP");
        }
    }

}
