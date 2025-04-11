package no.nav.foreldrepenger.abakus.registerdata.ytelse.kelvin;

import java.math.BigDecimal;
import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOG = LoggerFactory.getLogger(KelvinKlient.class);

    private final RestClient restClient;
    private final RestConfig restConfig;
    private final URI endpointHentAAP;

    public KelvinKlient() {
        this.restClient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        this.endpointHentAAP = restConfig.endpoint();
    }

    public List<MeldekortUtbetalingsgrunnlagSak> hentAAP(PersonIdent ident, LocalDate fom, LocalDate tom, int antallArena) {
        try {
            var body = new KelvinRequest(ident.getIdent(), fom, tom);
            var request = RestRequest.newPOSTJson(body, endpointHentAAP, restConfig);
            var result = restClient.send(request, ArbeidsavklaringspengerResponse.class);
            var resultAntall = result.vedtak().size();
            var kelvinVedtak = result.vedtak().stream().filter(v -> ArbeidsavklaringspengerResponse.Kildesystem.KELVIN.equals(v.kildesystem())).toList();
            var arenaVedtak = result.vedtak().stream().filter(v -> ArbeidsavklaringspengerResponse.Kildesystem.ARENA.equals(v.kildesystem())).toList();
            if (resultAntall > 0 || antallArena > 0) {
                LOG.info("Maksimum AAP Klient hentet {} vedtak - Kelvin {} Arena {} - mot mUG {} ", resultAntall, kelvinVedtak.size(), arenaVedtak.size(), antallArena);
            }
            if (!kelvinVedtak.isEmpty()) {
                LOG.warn("Merk Dem! De observerer nå et tilfelle der bruker mottar nye AAP. Meld fra til overvåkningen umiddelbart. Vedtak {}", kelvinVedtak);
            }
            return mapTilMeldekortAcl(result);
        } catch (Exception e) {
            LOG.info("Maksimum AAP Klient feil ved kall", e);
            return List.of();
        }
    }

    public record KelvinRequest(String personidentifikator, LocalDate fraOgMedDato, LocalDate tilOgMedDato) { }

    private static List<MeldekortUtbetalingsgrunnlagSak> mapTilMeldekortAcl(ArbeidsavklaringspengerResponse response) {
        return response.vedtak().stream()
            .map(KelvinKlient::mapTilMeldekortSakAcl)
            .sorted(Comparator.comparing(MeldekortUtbetalingsgrunnlagSak::getVedtaksPeriodeFom))
            .toList();
    }

    private static MeldekortUtbetalingsgrunnlagSak mapTilMeldekortSakAcl(ArbeidsavklaringspengerResponse.AAPVedtak vedtak) {
        var mk = vedtak.utbetaling().stream()
            .map(u -> KelvinKlient.mapTilMeldekortMKAcl(u, vedtak.kildesystem()))
            .sorted(Comparator.comparing(MeldekortUtbetalingsgrunnlagMeldekort::getMeldekortFom))
            .toList();
        return MeldekortUtbetalingsgrunnlagSak.MeldekortSakBuilder.ny()
            .leggTilMeldekort(mk)
            .medType(YtelseType.ARBEIDSAVKLARINGSPENGER)
            .medTilstand(YtelseStatus.LØPENDE)
            .medKilde(vedtak.kildesystem() == ArbeidsavklaringspengerResponse.Kildesystem.ARENA ? Fagsystem.ARENA : Fagsystem.KELVIN)
            .medSaksnummer(Optional.ofNullable(vedtak.saksnummer()).map(Saksnummer::new).orElse(null))
            .medSakStatus(vedtak.status())
            .medVedtakStatus(vedtak.status())
            .medKravMottattDato(vedtak.vedtaksdato())
            .medVedtattDato(vedtak.vedtaksdato())
            .medVedtaksPeriodeFom(Tid.fomEllerMin(vedtak.periode().fraOgMedDato()))
            .medVedtaksPeriodeTom(Tid.tomEllerMax(vedtak.periode().tilOgMedDato()))
            .medVedtaksDagsats(vedtak.dagsats() != null ? BigDecimal.valueOf(vedtak.dagsats()) : null)
            .build();
    }

    private static MeldekortUtbetalingsgrunnlagMeldekort mapTilMeldekortMKAcl(ArbeidsavklaringspengerResponse.AAPUtbetaling utbetaling,
                                                                              ArbeidsavklaringspengerResponse.Kildesystem kildesystem) {
        // OBS utbetaling / barnetillegg
        if (utbetaling.barnetillegg() != null && utbetaling.barnetillegg() > 0) {
            LOG.info("Maksimum AAP Klient har barnetillegg {}", utbetaling);
        }
        return MeldekortUtbetalingsgrunnlagMeldekort.MeldekortMeldekortBuilder.ny()
            .medMeldekortFom(Tid.fomEllerMin(utbetaling.periode().fraOgMedDato()))
            .medMeldekortTom(Tid.tomEllerMax(utbetaling.periode().tilOgMedDato()))
            .medBeløp(Optional.ofNullable(utbetaling.belop()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO))
            .medDagsats(Optional.ofNullable(utbetaling.dagsats()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO))
            .medUtbetalingsgrad(ArbeidsavklaringspengerResponse.Kildesystem.ARENA.equals(kildesystem)
                ? regnUtArenaUtbetalingsgrad(utbetaling) : Optional.ofNullable(utbetaling.utbetalingsgrad()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO))
            .build();
    }

    private static BigDecimal regnUtArenaUtbetalingsgrad(ArbeidsavklaringspengerResponse.AAPUtbetaling utbetaling) {
        var beløp = Optional.ofNullable(utbetaling.belop()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO);
        var dagsats = Optional.ofNullable(utbetaling.dagsats()).map(BigDecimal::valueOf).orElse(BigDecimal.ONE);
        var virkedager = beregnVirkedager(utbetaling.periode().fraOgMedDato(), utbetaling.periode().tilOgMedDato());
        return beløp.multiply(BigDecimal.valueOf(200)).divide(dagsats.multiply(BigDecimal.valueOf(virkedager)), 1, BigDecimal.ROUND_HALF_UP);
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

}
