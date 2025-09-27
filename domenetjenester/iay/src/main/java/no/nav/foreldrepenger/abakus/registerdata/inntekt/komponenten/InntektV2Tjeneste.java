package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, endpointProperty = "inntektbulk.url",
    endpointDefault = "http://ikomp.team-inntekt/rest/v2/inntekt/bulk",
    scopesProperty = "inntekt.scopes", scopesDefault = "api://prod-fss.team-inntekt.ikomp/.default")
public class InntektV2Tjeneste {

    // Dato for eldste request til inntk - det er av og til noen ES saker som spør lenger tilbake i tid
    private static final YearMonth INNTK_TIDLIGSTE_DATO = YearMonth.of(2015, 7);
    private static final Set<InntektskildeType> SKAL_PERIODISERE_INNTEKTSKILDE = Set.of(InntektskildeType.INNTEKT_SAMMENLIGNING,
        InntektskildeType.INNTEKT_BEREGNING);

    private static final Map<InntektskildeType, InntektsFilter> KILDE_TIL_FILTER = Map.of(
        InntektskildeType.INNTEKT_OPPTJENING, InntektsFilter.OPPTJENINGSGRUNNLAG,
        InntektskildeType.INNTEKT_BEREGNING, InntektsFilter.BEREGNINGSGRUNNLAG,
        InntektskildeType.INNTEKT_SAMMENLIGNING, InntektsFilter.SAMMENLIGNINGSGRUNNLAG);
    private static final Map<InntektsFilter, InntektskildeType> FILTER_TIL_KILDE = Map.of(
        InntektsFilter.OPPTJENINGSGRUNNLAG, InntektskildeType.INNTEKT_OPPTJENING,
        InntektsFilter.BEREGNINGSGRUNNLAG, InntektskildeType.INNTEKT_BEREGNING,
        InntektsFilter.SAMMENLIGNINGSGRUNNLAG, InntektskildeType.INNTEKT_SAMMENLIGNING);

    private final RestClient restClient;
    private final RestConfig restConfig;


    public InntektV2Tjeneste() {
        this(RestClient.client());
    }

    public InntektV2Tjeneste(RestClient restClient) {
        this(restClient, RestConfig.forClient(InntektV2Tjeneste.class));
    }

    public InntektV2Tjeneste(RestClient restClient, RestConfig config) {
        this.restClient = restClient;
        this.restConfig = config;
    }

    public Map<InntektskildeType, InntektsInformasjon> finnInntekt(FinnInntektRequest finnInntektRequest, Set<InntektskildeType> kilder) {
        var request = lagRequest(finnInntektRequest, kilder);

        try {
            var response = restClient.send(RestRequest.newPOSTJson(request, restConfig.endpoint(), restConfig), InntektBulkApiUt.class);
            return oversettResponse(response, kilder);
        } catch (RuntimeException e) {
            throw new IntegrasjonException("FP-824246",
                "Feil ved kall til inntektstjenesten. Meld til #team_registre og #produksjonshendelser hvis dette skjer over lengre tidsperiode.", e);
        }
    }

    private InntektBulkApiInn lagRequest(FinnInntektRequest finnInntektRequest, Set<InntektskildeType> kilder) {

        var ident = Optional.ofNullable(finnInntektRequest.getAktørId()).orElseGet(finnInntektRequest::getFnr);
        var filtre = kilder.stream().map(KILDE_TIL_FILTER::get).filter(Objects::nonNull).map(InntektsFilter::getKode).toList();

        return new InntektBulkApiInn(ident, filtre, InntektsFormål.FORMAAL_FORELDREPENGER.getKode(),
            brukDato(finnInntektRequest.getFom()), brukDato(finnInntektRequest.getTom()));
    }

    private static YearMonth brukDato(YearMonth dato) {
        return dato != null && dato.isAfter(INNTK_TIDLIGSTE_DATO) ? dato : INNTK_TIDLIGSTE_DATO;
    }

    private Map<InntektskildeType, InntektsInformasjon> oversettResponse(InntektBulkApiUt response, Set<InntektskildeType> kilder) {
        var svar = Optional.ofNullable(response).map(InntektBulkApiUt::bulk).orElseGet(List::of);
        if (svar.isEmpty()) {
            return Map.of();
        }

        var oversatt = new ArrayList<InntektsInformasjon>();
        for (var i : svar) {
            var kilde = FILTER_TIL_KILDE.getOrDefault(getFilter(i), InntektskildeType.UDEFINERT);
            if (!kilder.contains(kilde)) continue;
            var data = Optional.ofNullable(i.data()).orElseGet(List::of);
            List<Månedsinntekt> månedsinntekter = new ArrayList<>();
            for (var d : data) {
                månedsinntekter.addAll(oversettInntekter(d, kilde));
            }
            oversatt.add(new InntektsInformasjon(månedsinntekter, kilde));
        }

        return oversatt.stream().collect(Collectors.toMap(InntektsInformasjon::getKilde, i -> i));
    }

    private List<Månedsinntekt> oversettInntekter(Inntektsinformasjon inntektsinformasjon, InntektskildeType kilde) {
        List<Månedsinntekt> månedsinntekter = new ArrayList<>();
        var inntekter = Optional.ofNullable(inntektsinformasjon.inntektListe()).orElseGet(List::of);
        for (var inntekt : inntekter) {
            var brukYM = inntektsinformasjon.maaned();
            var tilleggsinformasjon = inntekt.tilleggsinformasjon();
            if (tilleggsinformasjon != null
                && "YtelseFraOffentlige".equals(inntekt.type())
                && "Etterbetalingsperiode".equals(tilleggsinformasjon.type())
                && tilleggsinformasjon.startdato() != null
                && skalPeriodisereInntektsKilde(kilde)) {
                brukYM = YearMonth.from(tilleggsinformasjon.startdato().plusDays(1));
            }
            var månedsinntekt = new Månedsinntekt.Builder()
                .medBeløp(inntekt.beloep())
                .medSkatteOgAvgiftsregelType(inntekt.skatteOgAvgiftsregel());

            if (brukYM != null) {
                månedsinntekt.medMåned(brukYM);
            }
            utledOgSettUtbetalerOgYtelse(inntektsinformasjon, inntekt, månedsinntekt);

            månedsinntekter.add(månedsinntekt.build());

        }
        return månedsinntekter;
    }


    private boolean skalPeriodisereInntektsKilde(InntektskildeType kilde) {
        return SKAL_PERIODISERE_INNTEKTSKILDE.contains(kilde);
    }

    private void utledOgSettUtbetalerOgYtelse(Inntektsinformasjon inntektsinformasjon, Inntekt inntekt, Månedsinntekt.Builder månedsinntekt) {
        switch (inntekt.type()) {
            case "YtelseFraOffentlige" -> månedsinntekt.medYtelse(true).medYtelseKode(inntekt.beskrivelse());
            case "PensjonEllerTrygd" -> månedsinntekt.medYtelse(true).medPensjonEllerTrygdKode(inntekt.beskrivelse());
            case "Naeringsinntekt" -> månedsinntekt.medYtelse(true).medNæringsinntektKode(inntekt.beskrivelse());
            case "Loennsinntekt" -> {
                månedsinntekt.medYtelse(false);
                månedsinntekt.medArbeidsgiver(inntektsinformasjon.underenhet());
                månedsinntekt.medLønnsbeskrivelseKode(inntekt.beskrivelse());
            }
            case null, default -> throw new TekniskException("FP-711674", String.format("Kunne ikke mappe svar fra Inntektskomponenten: virksomhet=%s, inntektType=%s",
                inntektsinformasjon.underenhet(), inntekt.type()));
        }
    }


    private static InntektsFilter getFilter(InntektBulk bulk) {
        return Arrays.stream(InntektsFilter.values())
            .filter(f -> f.getKode().equals(bulk.filter()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(String.format("Ugyldig filter i ikomp-respons %s ", bulk.filter())));
    }

    public record InntektBulkApiInn(String personident, List<String> filter, String formaal,
                                    YearMonth maanedFom, YearMonth maanedTom) { }

    public record InntektBulkApiUt(List<InntektBulk> bulk) { }

    public record InntektBulk(String filter, List<Inntektsinformasjon> data) { }

    public record Inntektsinformasjon(YearMonth maaned, String opplysningspliktig, String underenhet, List<Inntekt> inntektListe) { }

    public record Inntekt(String type, BigDecimal beloep, String beskrivelse, String skatteOgAvgiftsregel, Tilleggsinformasjon tilleggsinformasjon) { }

    public record Tilleggsinformasjon(String type, LocalDate startdato, LocalDate sluttdato) { }
}
