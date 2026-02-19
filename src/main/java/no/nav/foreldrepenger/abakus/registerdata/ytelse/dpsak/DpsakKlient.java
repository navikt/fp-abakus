package no.nav.foreldrepenger.abakus.registerdata.ytelse.dpsak;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.core.UriBuilder;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@Dependent
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC,
    endpointProperty = "dagpengerdatadeling.base.url", endpointDefault = "https://dp-datadeling.intern.nav.no",
    scopesProperty = "dagpengerdatadeling.scopes", scopesDefault = "api://prod-gcp.teamdagpenger.dp-datadeling/.default")
public class DpsakKlient {

    private static final Logger LOG = LoggerFactory.getLogger(DpsakKlient.class);

    private final RestClient restClient;
    private final RestConfig restConfig;
    private final URI perioderEndpoint;
    private final URI utbetalingEndpoint;

    public DpsakKlient() {
        this.restClient = RestClient.client();
        this.restConfig = RestConfig.forClient(DpsakKlient.class);
        this.perioderEndpoint = UriBuilder.fromUri(restConfig.endpoint()).path("/dagpenger/datadeling/v1/perioder").build();
        this.utbetalingEndpoint = UriBuilder.fromUri(restConfig.endpoint()).path("/dagpenger/datadeling/v1/beregninger").build();
    }

    public void hentDagpenger(PersonIdent personIdent, LocalDate fom, LocalDate tom, Saksnummer sak) {
        try {
            var perioder = hentRettighetsperioder(personIdent, fom, tom);
            var utbetalinger = hentUtbetalinger(personIdent, fom, tom);
            var perioderArena = perioder.stream()
                .filter(p -> DagpengerRettighetsperioderDto.DagpengerKilde.ARENA.equals(p.kilde()))
                .toList();
            var perioderDpsak = perioder.stream()
                .filter(p -> DagpengerRettighetsperioderDto.DagpengerKilde.DP_SAK.equals(p.kilde()))
                .toList();
            var utbetalingerDpsak = utbetalinger.stream()
                .filter(u -> DagpengerKilde.DP_SAK.equals(u.kilde()))
                .toList();
            var utbetalingerArena = utbetalingerDpsak.stream()
                .filter(u -> DagpengerKilde.ARENA.equals(u.kilde()))
                .toList();
            // Skru ned til å logge treff når validert
            if (!perioderArena.isEmpty() || !utbetalingerArena.isEmpty()) {
                LOG.info("DP-DATADELING ARENA fant {} perioder og {} utbetalinger", perioderArena.size(), utbetalingerArena.size());
            }
            if (!perioderDpsak.isEmpty() || !utbetalingerDpsak.isEmpty()) {
                LOG.info("DP-DATADELING DPSAK fant {} perioder og {} utbetalinger", perioderDpsak.size(), utbetalingerDpsak.size());
                LOG.info("Merk Dem! Sak {} har nye dagpenger. Kontakt produkteier umiddelbart", sak.getVerdi());
            }
        } catch (Exception e) {
            LOG.info("DP-DATADELING feil ", e);
        }
    }

    public List<DagpengerRettighetsperioderDto.Rettighetsperiode> hentRettighetsperioder(PersonIdent personIdent, LocalDate fom, LocalDate tom) {
        var prequest = new PersonRequest(personIdent.getIdent(), fom, tom);
        var rrequest = RestRequest.newPOSTJson(prequest, perioderEndpoint, restConfig);
        return restClient.sendReturnOptional(rrequest, DagpengerRettighetsperioderDto.class)
            .map(DagpengerRettighetsperioderDto::perioder)
            .orElseGet(List::of);
    }

    public List<DagpengerUtbetalingDto> hentUtbetalinger(PersonIdent personIdent, LocalDate fom, LocalDate tom) {
        var prequest = new PersonRequest(personIdent.getIdent(), fom, tom);
        var rrequest = RestRequest.newPOSTJson(prequest, utbetalingEndpoint, restConfig);
        return restClient.sendReturnList(rrequest, DagpengerUtbetalingDto.class);
    }

    public record PersonRequest(String personIdent, LocalDate fraOgMedDato, LocalDate tilOgMedDato) { }


}
