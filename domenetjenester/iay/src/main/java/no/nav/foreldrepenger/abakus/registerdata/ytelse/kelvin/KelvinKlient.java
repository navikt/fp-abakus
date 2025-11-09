package no.nav.foreldrepenger.abakus.registerdata.ytelse.kelvin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.enterprise.context.Dependent;
import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagMeldekort;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.konfig.Tid;

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

    public Map<Fagsystem, List<MeldekortUtbetalingsgrunnlagSak>> hentAAP(PersonIdent ident, LocalDate fom, LocalDate tom) {
        var body = new KelvinRequest(ident.getIdent(), fom, tom);
        var request = RestRequest.newPOSTJson(body, endpointHentAAP, restConfig);
        var result = restClient.send(request, ArbeidsavklaringspengerResponse.class);
        var kelvinVedtak = result.vedtak().stream().filter(v -> ArbeidsavklaringspengerResponse.Kildesystem.KELVIN.equals(v.kildesystem())).toList();
        var arenaVedtak = result.vedtak().stream().filter(v -> ArbeidsavklaringspengerResponse.Kildesystem.ARENA.equals(v.kildesystem())).toList();
        return Map.of(Fagsystem.ARENA, mapTilMeldekortAcl(arenaVedtak), Fagsystem.KELVIN, mapTilMeldekortAcl(kelvinVedtak));
    }

    public record KelvinRequest(String personidentifikator, LocalDate fraOgMedDato, LocalDate tilOgMedDato) { }

    private static List<MeldekortUtbetalingsgrunnlagSak> mapTilMeldekortAcl(List<ArbeidsavklaringspengerResponse.AAPVedtak> vedtak) {
        return vedtak.stream()
            .map(KelvinKlient::mapTilMeldekortSakAcl)
            .sorted(Comparator.comparing(MeldekortUtbetalingsgrunnlagSak::getVedtaksPeriodeFom))
            .toList();
    }

    private static MeldekortUtbetalingsgrunnlagSak mapTilMeldekortSakAcl(ArbeidsavklaringspengerResponse.AAPVedtak vedtak) {
        return switch (vedtak.kildesystem()) {
            case ARENA -> mapTilMeldekortSakAclArena(vedtak);
            case KELVIN -> mapTilMeldekortSakAclKelvin(vedtak);
        };
    }

    private static MeldekortUtbetalingsgrunnlagSak mapTilMeldekortSakAclArena(ArbeidsavklaringspengerResponse.AAPVedtak vedtak) {
        var mk = vedtak.utbetaling().stream()
            .map(u -> KelvinKlient.mapTilMeldekortMKAclArena(u))
            .sorted(Comparator.comparing(MeldekortUtbetalingsgrunnlagMeldekort::getMeldekortFom))
            .toList();
        // Kan hende barnetillegg må ganges med barnMedStonad
        var vedtaksdagsats = Optional.ofNullable(vedtak.dagsatsEtterUføreReduksjon()).or(() -> Optional.ofNullable(vedtak.dagsats())).orElse(0);
        var vedtaksdagsatsMedBarnetillegg = vedtaksdagsats + Optional.ofNullable(vedtak.barnetillegg()).orElse(0);
        return MeldekortUtbetalingsgrunnlagSak.MeldekortSakBuilder.ny()
            .leggTilMeldekort(mk)
            .medType(YtelseType.ARBEIDSAVKLARINGSPENGER)
            .medTilstand(tilTilstand(vedtak.status()))
            .medKilde(Fagsystem.ARENA)
            .medSaksnummer(Optional.ofNullable(vedtak.saksnummer()).map(Saksnummer::new).orElse(null))
            .medKravMottattDato(vedtak.vedtaksdato())
            .medVedtattDato(vedtak.vedtaksdato())
            .medVedtaksPeriodeFom(Tid.fomEllerMin(vedtak.periode().fraOgMedDato()))
            .medVedtaksPeriodeTom(Tid.tomEllerMax(vedtak.periode().tilOgMedDato()))
            .medVedtaksDagsats(BigDecimal.valueOf(vedtaksdagsatsMedBarnetillegg))
            .build();
    }

    private static MeldekortUtbetalingsgrunnlagMeldekort mapTilMeldekortMKAclArena(ArbeidsavklaringspengerResponse.AAPUtbetaling utbetaling) {
        return MeldekortUtbetalingsgrunnlagMeldekort.MeldekortMeldekortBuilder.ny()
            .medMeldekortFom(Tid.fomEllerMin(utbetaling.periode().fraOgMedDato()))
            .medMeldekortTom(Tid.tomEllerMax(utbetaling.periode().tilOgMedDato()))
            .medBeløp(Optional.ofNullable(utbetaling.belop()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO))
            .medDagsats(Optional.ofNullable(utbetaling.dagsats()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO))
            .medUtbetalingsgrad(regnUtArenaUtbetalingsgrad(utbetaling))
            .build();
    }

    private static MeldekortUtbetalingsgrunnlagSak mapTilMeldekortSakAclKelvin(ArbeidsavklaringspengerResponse.AAPVedtak vedtak) {
        var aktuellDagsats = Optional.ofNullable(vedtak.dagsatsEtterUføreReduksjon()).orElseGet(vedtak::dagsats);
        var vedtaksdagsatsMedBarnetillegg = aktuellDagsats + Optional.ofNullable(vedtak.barnetillegg()).orElse(0);
        var mk = vedtak.utbetaling().stream()
            .map(u -> KelvinKlient.mapTilMeldekortMKAclKelvin(u, aktuellDagsats, vedtak.dagsats()))
            .sorted(Comparator.comparing(MeldekortUtbetalingsgrunnlagMeldekort::getMeldekortFom))
            .toList();
        return MeldekortUtbetalingsgrunnlagSak.MeldekortSakBuilder.ny()
            .leggTilMeldekort(mk)
            .medType(YtelseType.ARBEIDSAVKLARINGSPENGER)
            .medTilstand(tilTilstand(vedtak.status()))
            .medKilde(Fagsystem.KELVIN)
            .medSaksnummer(Optional.ofNullable(vedtak.saksnummer()).map(Saksnummer::new).orElse(null))
            .medKravMottattDato(vedtak.vedtaksdato())
            .medVedtattDato(vedtak.vedtaksdato())
            .medVedtaksPeriodeFom(Tid.fomEllerMin(vedtak.periode().fraOgMedDato()))
            .medVedtaksPeriodeTom(Tid.tomEllerMax(vedtak.periode().tilOgMedDato()))
            .medVedtaksDagsats(BigDecimal.valueOf(vedtaksdagsatsMedBarnetillegg))
            .build();
    }

    private static MeldekortUtbetalingsgrunnlagMeldekort mapTilMeldekortMKAclKelvin(ArbeidsavklaringspengerResponse.AAPUtbetaling utbetaling,
                                                                                    Integer aktuellDagsats, Integer vedtakDagsats) {
        // Her vil tilfelle med uførereduksjon ha en ubetalingsgrad mellom 0 og 100 gitt av uførereduksjonen + aktivitet i perioden
        // Gjør derfor en normalisering slik at bruker med 60% AAP får utbetalingsgrad 100% ved full AAP-utbetaling uten aktivitet
        var utbetalingsgradFraUtbetaling = Optional.ofNullable(utbetaling.utbetalingsgrad()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO);
        var brukUtbetalingsgrad = utbetalingsgradFraUtbetaling
            .multiply(BigDecimal.valueOf(vedtakDagsats))
            .divide(BigDecimal.valueOf(aktuellDagsats), 0, RoundingMode.HALF_EVEN);
        var dagsats = Optional.ofNullable(utbetaling.dagsats()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO)
            .add(Optional.ofNullable(utbetaling.barnetillegg()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO));
        return MeldekortUtbetalingsgrunnlagMeldekort.MeldekortMeldekortBuilder.ny()
            .medMeldekortFom(Tid.fomEllerMin(utbetaling.periode().fraOgMedDato()))
            .medMeldekortTom(Tid.tomEllerMax(utbetaling.periode().tilOgMedDato()))
            .medBeløp(Optional.ofNullable(utbetaling.belop()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO))
            .medDagsats(dagsats)
            .medUtbetalingsgrad(brukUtbetalingsgrad)
            .build();
    }

    private static BigDecimal regnUtArenaUtbetalingsgrad(ArbeidsavklaringspengerResponse.AAPUtbetaling utbetaling) {
        var beløp = Optional.ofNullable(utbetaling.belop()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO);
        var dagsats = Optional.ofNullable(utbetaling.dagsats()).map(BigDecimal::valueOf).orElse(BigDecimal.ONE);
        var virkedager = beregnVirkedager(utbetaling.periode().fraOgMedDato(), utbetaling.periode().tilOgMedDato());
        return beløp.multiply(BigDecimal.valueOf(200)).divide(dagsats.multiply(BigDecimal.valueOf(virkedager)), 1, RoundingMode.HALF_UP);
    }

    private static int beregnVirkedager(LocalDate fom, LocalDate tom) {
        try {
            var padBefore = fom.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
            var padAfter = DayOfWeek.SUNDAY.getValue() - tom.getDayOfWeek().getValue();
            var virkedagerPadded = Math.toIntExact(
                ChronoUnit.WEEKS.between(fom.minusDays(padBefore), tom.plusDays(padAfter).plusDays(1L)) * 5L);
            var virkedagerPadding = Math.min(padBefore, 5) + Math.max(padAfter - 2, 0);
            return virkedagerPadded - virkedagerPadding;
        } catch (ArithmeticException var6) {
            throw new UnsupportedOperationException("Perioden er for lang til å beregne virkedager.", var6);
        }
    }

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
