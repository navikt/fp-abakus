package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, endpointProperty = "inntektbulk.url", endpointDefault = "http://ikomp.team-inntekt/rest/v2/inntekt/bulk", scopesProperty = "inntekt.scopes", scopesDefault = "api://prod-fss.team-inntekt.ikomp/.default")
public class InntektTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(InntektTjeneste.class);

    private static final String FORMAAL_FORELDREPENGER = "Foreldrepenger";
    private static final String TILLEGG_ETTERBETALING = "Etterbetalingsperiode";

    // Dato for eldste request til inntk - det er av og til noen ES saker som spør lenger tilbake i tid
    private static final YearMonth INNTK_TIDLIGSTE_DATO = YearMonth.of(2015, 7);
    private static final Set<InntektskildeType> SKAL_PERIODISERE_INNTEKTSKILDE = Set.of(InntektskildeType.INNTEKT_SAMMENLIGNING,
        InntektskildeType.INNTEKT_BEREGNING);

    private static final Map<InntektskildeType, Inntektsfilter> KILDE_TIL_FILTER = Map.of(InntektskildeType.INNTEKT_OPPTJENING,
        Inntektsfilter.OPPTJENINGSGRUNNLAG, InntektskildeType.INNTEKT_BEREGNING, Inntektsfilter.BEREGNINGSGRUNNLAG,
        InntektskildeType.INNTEKT_SAMMENLIGNING, Inntektsfilter.SAMMENLIGNINGSGRUNNLAG);
    private static final Map<Inntektsfilter, InntektskildeType> FILTER_TIL_KILDE = Map.of(Inntektsfilter.OPPTJENINGSGRUNNLAG,
        InntektskildeType.INNTEKT_OPPTJENING, Inntektsfilter.BEREGNINGSGRUNNLAG, InntektskildeType.INNTEKT_BEREGNING,
        Inntektsfilter.SAMMENLIGNINGSGRUNNLAG, InntektskildeType.INNTEKT_SAMMENLIGNING);

    private final RestClient restClient;
    private final RestConfig restConfig;


    public InntektTjeneste() {
        this(RestClient.client());
    }

    public InntektTjeneste(RestClient restClient) {
        this(restClient, RestConfig.forClient(InntektTjeneste.class));
    }

    public InntektTjeneste(RestClient restClient, RestConfig config) {
        this.restClient = restClient;
        this.restConfig = config;
    }

    private static YearMonth brukDato(YearMonth dato) {
        return dato != null && dato.isAfter(INNTK_TIDLIGSTE_DATO) ? dato : INNTK_TIDLIGSTE_DATO;
    }

    private static List<Månedsinntekt> utledOgOpprettMånedsinntekter(Inntektsinformasjon inntektsinformasjon,
                                                                     InntektskildeType kilde,
                                                                     Inntekt inntekt) {
        var måned = inntektsinformasjon.maaned();
        var tilleggsinformasjon = inntekt.tilleggsinformasjon();
        var arbeidsgiver = Inntektstype.LØNN.equals(inntekt.type()) ? inntektsinformasjon.underenhet() : null;

        if (!skalPeriodisereInntekt(kilde, tilleggsinformasjon, inntekt.type())) {
            return List.of(new Månedsinntekt(inntekt.type, måned, inntekt.beloep, inntekt.beskrivelse, arbeidsgiver, inntekt.skatteOgAvgiftsregel));
        }

        if (erEtterbetalingForFlereMåneder(tilleggsinformasjon)) {
            return fordelUtbetalingPerMåned(kilde, inntekt, måned, tilleggsinformasjon, arbeidsgiver);
        } else {
            måned = YearMonth.from(tilleggsinformasjon.startdato().plusDays(1));
            return List.of(new Månedsinntekt(inntekt.type, måned, inntekt.beloep, inntekt.beskrivelse, arbeidsgiver, inntekt.skatteOgAvgiftsregel));
        }
    }

    private static boolean erEtterbetalingForFlereMåneder(Tilleggsinformasjon tilleggsinformasjon) {
        return !Objects.equals(YearMonth.from(tilleggsinformasjon.startdato()), YearMonth.from(tilleggsinformasjon.sluttdato()));
    }

    private static List<Månedsinntekt> fordelUtbetalingPerMåned(InntektskildeType kilde,
                                                         Inntekt inntekt,
                                                         YearMonth brukYM,
                                                         Tilleggsinformasjon tilleggsinformasjon,
                                                         String arbeidsgiver) {
        LOG.info(
            "InntektTjeneste etterbetaling flere måneder: inntektskildeType {} ytelse {} utbetalingsmåned {} etterbetaling fra-dato {} etterbetaling tom-dato {}",
            kilde, inntekt.beskrivelse(), brukYM, tilleggsinformasjon.startdato(), tilleggsinformasjon.sluttdato());

        var månedMedVirkedagerListe = utledMånedOgvirkedagerForPeriode(tilleggsinformasjon.startdato().plusDays(1),
            tilleggsinformasjon.sluttdato());
        var månedsinntekter = finnMånedsinntektForAlleMåneder(inntekt, månedMedVirkedagerListe, arbeidsgiver);

        månedsinntekter.forEach(
            månedsinntekt -> LOG.info("InntektTjeneste etterbetaling flere måneder: fordelt beløp per måned: måned {} beløp {}",
                månedsinntekt.måned(), månedsinntekt.beløp()));

        return månedsinntekter;
    }

    private static boolean skalPeriodisereInntekt(InntektskildeType kilde, Tilleggsinformasjon tilleggsinformasjon, Inntektstype inntektType) {
        return tilleggsinformasjon != null && Inntektstype.YTELSE.equals(inntektType) && TILLEGG_ETTERBETALING.equals(tilleggsinformasjon.type())
            && tilleggsinformasjon.startdato() != null && SKAL_PERIODISERE_INNTEKTSKILDE.contains(kilde);
    }

    public static List<MånedMedVirkedager> utledMånedOgvirkedagerForPeriode(LocalDate fraDato, LocalDate tilDato) {
        return Stream.iterate(YearMonth.from(fraDato), ym -> !ym.isAfter(YearMonth.from(tilDato)), ym -> ym.plusMonths(1))
            .map(månedSomSjekkes -> {
            int antallVirkedager = finnAntallVirkedagerForMåned(fraDato, tilDato, månedSomSjekkes);
            LOG.info("InntektTjeneste etterbetaling flere måneder: måned: {} antall virkedager {}", månedSomSjekkes, antallVirkedager);
            return new MånedMedVirkedager(månedSomSjekkes, antallVirkedager);
        }).toList();
    }

    private static int finnAntallVirkedagerForMåned(LocalDate periodeFraDato, LocalDate periodeTilDato, YearMonth månedSomSjekkes) {
        LocalDate fraDatoIMnd;
        LocalDate tilDatoIMnd;

        // Her bestemmes hvilken dag i måneden som sjekkes starter og stopper
        if (månedSomSjekkes.equals(YearMonth.from(periodeFraDato))) {
            //dersom vi er i samme måned som periodeFraDato skal perioden starte med periodeFraDato og til siste dag i den måneden
            fraDatoIMnd = periodeFraDato;
            tilDatoIMnd = YearMonth.from(fraDatoIMnd).atEndOfMonth();
        } else if (månedSomSjekkes.equals(YearMonth.from(periodeTilDato))) {
            //dersom vi er i samme måned som periodeTilDato skal perioden starte første dagen i den måneden og gå til periodeTilDato
            fraDatoIMnd = månedSomSjekkes.atDay(1);
            tilDatoIMnd = periodeTilDato;
        } else {
            //hele måneden når det ikke er start- eller sluttdato
            fraDatoIMnd = månedSomSjekkes.atDay(1);
            tilDatoIMnd = YearMonth.from(fraDatoIMnd).atEndOfMonth();
        }
        //teller antall virkedager i perioden
        return (int) fraDatoIMnd.datesUntil(tilDatoIMnd.plusDays(1))
            .filter(InntektTjeneste::erVirkedag)
            .count();
    }

    private static boolean erVirkedag(LocalDate date) {
        // Søndag eller lørdag
        return date.getDayOfWeek().getValue() < 6;
    }

    private static List<Månedsinntekt> finnMånedsinntektForAlleMåneder(Inntekt inntekt,
                                                                       List<MånedMedVirkedager> månedOgVirkedagerListe,
                                                                       String arbeidsgiver) {
        var månedsinntekter = new ArrayList<Månedsinntekt>();
        var sumVirkedager = månedOgVirkedagerListe.stream()
            .mapToInt(MånedMedVirkedager::antallVirkedager)
            .sum();

        var dagsats = inntekt.beloep.divide(BigDecimal.valueOf(sumVirkedager), 10, RoundingMode.HALF_UP);
        månedOgVirkedagerListe.forEach(månedMedVirkedager -> {
            var beløpForMåneden = dagsats.multiply(BigDecimal.valueOf(månedMedVirkedager.antallVirkedager));
            månedsinntekter.add(new Månedsinntekt(inntekt.type(), månedMedVirkedager.måned(), beløpForMåneden, inntekt.beskrivelse(), arbeidsgiver,
                inntekt.skatteOgAvgiftsregel));
        });
        return månedsinntekter;
    }

    public Map<InntektskildeType, InntektsInformasjon> finnInntekt(String ident, YearMonth fom, YearMonth tom, Set<InntektskildeType> kilder) {
        var filtre = kilder.stream().map(KILDE_TIL_FILTER::get).filter(Objects::nonNull).toList();

        var request = new InntektBulkApiInn(ident, filtre, FORMAAL_FORELDREPENGER, brukDato(fom), brukDato(tom));

        try {
            var response = restClient.send(RestRequest.newPOSTJson(request, restConfig.endpoint(), restConfig), InntektBulkApiUt.class);
            return oversettResponse(response, kilder);
        } catch (RuntimeException e) {
            throw new IntegrasjonException("FP-824246",
                "Feil ved kall til inntektstjenesten. Meld til #team_registre og #produksjonshendelser hvis dette skjer over lengre tidsperiode.", e);
        }
    }

    private Map<InntektskildeType, InntektsInformasjon> oversettResponse(InntektBulkApiUt response, Set<InntektskildeType> kilder) {
        var svar = Optional.ofNullable(response).map(InntektBulkApiUt::bulk).orElseGet(List::of);
        if (svar.isEmpty()) {
            return Map.of();
        }

        var oversatt = new ArrayList<InntektsInformasjon>();
        for (var i : svar) {
            var kilde = FILTER_TIL_KILDE.getOrDefault(i.filter(), InntektskildeType.UDEFINERT);
            if (!kilder.contains(kilde)) {
                continue;
            }
            var data = Optional.ofNullable(i.data()).orElseGet(List::of);
            List<Månedsinntekt> månedsinntekter = new ArrayList<>();
            for (var d : data) {
                månedsinntekter.addAll(oversettInntekter(d, kilde));
            }
            oversatt.add(new InntektsInformasjon(månedsinntekter, kilde));
        }

        return oversatt.stream().collect(Collectors.toMap(InntektsInformasjon::kilde, i -> i));
    }

    private List<Månedsinntekt> oversettInntekter(Inntektsinformasjon inntektsinformasjon, InntektskildeType kilde) {
        return Optional.ofNullable(inntektsinformasjon.inntektListe())
            .orElseGet(List::of)
            .stream()
            .map(inntekt -> utledOgOpprettMånedsinntekter(inntektsinformasjon, kilde, inntekt))
            .flatMap(List::stream)
            .toList();
    }

    public record MånedMedVirkedager(YearMonth måned, int antallVirkedager) {
    }

    public record InntektBulkApiInn(String personident, List<Inntektsfilter> filter, String formaal, YearMonth maanedFom, YearMonth maanedTom) {
    }

    public record InntektBulkApiUt(List<InntektBulk> bulk) {
    }

    public record InntektBulk(Inntektsfilter filter, List<Inntektsinformasjon> data) {
    }

    public record Inntektsinformasjon(YearMonth maaned, String opplysningspliktig, String underenhet, List<Inntekt> inntektListe) {
    }

    public record Inntekt(Inntektstype type, BigDecimal beloep, String beskrivelse, String skatteOgAvgiftsregel,
                          Tilleggsinformasjon tilleggsinformasjon) {
    }

    public record Tilleggsinformasjon(String type, LocalDate startdato, LocalDate sluttdato) {
    }
}
