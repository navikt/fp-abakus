package no.nav.foreldrepenger.abakus.registerdata.ytelse.kelvin;

import java.net.URI;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@Dependent
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC,
    endpointProperty = "kelvin.maksimum.url", endpointDefault = "https://aap-api.intern.nav.no/maksimum",
    scopesProperty = "kelvin.maksimum.scopes", scopesDefault = "api://prod-gcp.aap.api-intern/.default")
public class KelvinKlient {

    private static final Logger LOG = LoggerFactory.getLogger(KelvinKlient.class);

    private final RestClient restClient;
    private final RestConfig restConfig;
    private final URI endpointHentAAP;

    public KelvinKlient() {
        this.restClient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        this.endpointHentAAP = restConfig.endpoint();
    }

    public void hentAAP(PersonIdent ident, LocalDate fom, LocalDate tom) {
        try {
            LOG.info("Henter dagpenger/AAP for {} i periode fom {} tom {}", ident, fom, tom);
            var body = new KelvinRequest(ident.getIdent(), fom, tom);
            var request = RestRequest.newPOSTJson(body, endpointHentAAP, restConfig);
            var result = restClient.send(request, ArbeidsavklaringspengerResponse.class);
            var kelvinVedtak = result.vedtak().stream().filter(v -> ArbeidsavklaringspengerResponse.Kildesystem.KELVIN.equals(v.kildesystem())).toList();
            var arenaVedtak = result.vedtak().stream().filter(v -> ArbeidsavklaringspengerResponse.Kildesystem.ARENA.equals(v.kildesystem())).toList();
            LOG.info("Maksimum AAP hentet {} vedtak fra Kelvin og {} vedtak fra Arena", kelvinVedtak.size(), arenaVedtak.size());
            if (!kelvinVedtak.isEmpty()) {
                LOG.warn("Merk Dem! De observerer nå et tilfelle der bruker mottar nye AAP. Meld fra til overvåkningen umiddelbart. Vedtak {}", kelvinVedtak);
            }
        } catch (Exception e) {
            LOG.info("Maksimum AAP feil ved kall", e);
        }
    }

    public record KelvinRequest(String personidentifikator, LocalDate fraOgMedDato, LocalDate tilOgMedDato) { }

}
