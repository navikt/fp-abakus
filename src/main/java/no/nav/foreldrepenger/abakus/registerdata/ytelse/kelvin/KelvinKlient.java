package no.nav.foreldrepenger.abakus.registerdata.ytelse.kelvin;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.Dependent;
import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
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

    private final RestClient restClient;
    private final RestConfig restConfig;
    private final URI endpointHentAAP;

    public KelvinKlient() {
        this.restClient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        this.endpointHentAAP = restConfig.endpoint();
    }

    public Map<Fagsystem, List<MeldekortUtbetalingsgrunnlagSak>> hentAAP(PersonIdent ident, LocalDate fom, LocalDate tom, Saksnummer saksnummer) {
        var body = new KelvinRequest(ident.getIdent(), fom, tom);
        var request = RestRequest.newPOSTJson(body, endpointHentAAP, restConfig);
        var result = restClient.send(request, ArbeidsavklaringspengerResponse.class);
        var kelvinVedtak = result.vedtak().stream().filter(v -> ArbeidsavklaringspengerResponse.Kildesystem.KELVIN.equals(v.kildesystem())).toList();
        var arenaVedtak = result.vedtak().stream().filter(v -> ArbeidsavklaringspengerResponse.Kildesystem.ARENA.equals(v.kildesystem())).toList();
        var kelvinMapped = KelvinMapper.mapTilMeldekortAclKelvin(kelvinVedtak, saksnummer);
        var arenaMapped = ArenaMapper.mapTilMeldekortAclArena(arenaVedtak, fom);
        return Map.of(Fagsystem.ARENA, arenaMapped, Fagsystem.KELVIN, kelvinMapped);
    }

    public record KelvinRequest(String personidentifikator, LocalDate fraOgMedDato, LocalDate tilOgMedDato) { }


    static YtelseStatus tilTilstand(String status) {
        return switch (status) {
            case "AVSLUTTET", "AVSLU" -> YtelseStatus.AVSLUTTET;
            case "LØPENDE", "IVERK" -> YtelseStatus.LØPENDE;
            case "UTREDES", "GODKJ", "INNST", "FORDE", "REGIS", "MOTAT", "KONT" -> YtelseStatus.UNDER_BEHANDLING;
            case "OPPRETTET", "OPPRE" -> YtelseStatus.OPPRETTET;
            case null, default -> YtelseStatus.UDEFINERT;
        };
    }

}
