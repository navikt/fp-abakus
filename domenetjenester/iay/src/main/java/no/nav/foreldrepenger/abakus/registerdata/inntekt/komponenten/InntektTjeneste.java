package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten;

import java.math.BigDecimal;
import java.net.URI;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidType;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.aktor.AktørTjeneste;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.tjenester.aordningen.inntektsinformasjon.Aktoer;
import no.nav.tjenester.aordningen.inntektsinformasjon.AktoerType;
import no.nav.tjenester.aordningen.inntektsinformasjon.ArbeidsInntektIdent;
import no.nav.tjenester.aordningen.inntektsinformasjon.ArbeidsInntektInformasjon;
import no.nav.tjenester.aordningen.inntektsinformasjon.ArbeidsInntektMaaned;
import no.nav.tjenester.aordningen.inntektsinformasjon.ArbeidsforholdFrilanser;
import no.nav.tjenester.aordningen.inntektsinformasjon.Tilleggsinformasjon;
import no.nav.tjenester.aordningen.inntektsinformasjon.inntekt.Inntekt;
import no.nav.tjenester.aordningen.inntektsinformasjon.inntekt.InntektType;
import no.nav.tjenester.aordningen.inntektsinformasjon.request.HentInntektListeBolkRequest;
import no.nav.tjenester.aordningen.inntektsinformasjon.response.HentInntektListeBolkResponse;
import no.nav.tjenester.aordningen.inntektsinformasjon.tilleggsinformasjondetaljer.Etterbetalingsperiode;
import no.nav.tjenester.aordningen.inntektsinformasjon.tilleggsinformasjondetaljer.TilleggsinformasjonDetaljerType;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.CONTEXT, endpointProperty = "hentinntektlistebolk.url")
public class InntektTjeneste {

    // Dato for eldste request til inntk - det er av og til noen ES saker som spør lenger tilbake i tid
    private static final YearMonth INNTK_TIDLIGSTE_DATO = YearMonth.of(2015, 7);
    private static final Set<InntektskildeType> SKAL_PERIODISERE_INNTEKTSKILDE = Set.of(InntektskildeType.INNTEKT_SAMMENLIGNING, InntektskildeType.INNTEKT_BEREGNING);

    private static final String ENDPOINT_KEY = "hentinntektlistebolk.url";

    private static final Logger logger = LoggerFactory.getLogger(InntektTjeneste.class);

    private RestClient restClient;
    private URI endpoint;
    private AktørTjeneste aktørConsumer;
    private Map<InntektskildeType, InntektsFilter> kildeTilFilter;

    InntektTjeneste() {
        // For CDI proxy
    }

    @Inject
    public InntektTjeneste(@KonfigVerdi(ENDPOINT_KEY) URI endpoint,
                           RestClient restClient,
                           AktørTjeneste aktørConsumer) {
        this.endpoint = endpoint;
        this.restClient = restClient;
        this.aktørConsumer = aktørConsumer;
        this.kildeTilFilter = Map.of(InntektskildeType.INNTEKT_OPPTJENING, InntektsFilter.OPPTJENINGSGRUNNLAG,
            InntektskildeType.INNTEKT_BEREGNING, InntektsFilter.BEREGNINGSGRUNNLAG,
            InntektskildeType.INNTEKT_SAMMENLIGNING, InntektsFilter.SAMMENLIGNINGSGRUNNLAG);
    }

    public InntektsInformasjon finnInntekt(FinnInntektRequest finnInntektRequest, InntektskildeType kilde, YtelseType ytelse) {
        HentInntektListeBolkResponse response = finnInntektRaw(finnInntektRequest, kilde);
        return oversettResponse(response, kilde, ytelse);

    }

    public HentInntektListeBolkResponse finnInntektRaw(FinnInntektRequest finnInntektRequest, InntektskildeType kilde) {
        var request = lagRequest(finnInntektRequest, kilde);

        HentInntektListeBolkResponse response;
        try {
            response = restClient.send(request, HentInntektListeBolkResponse.class);
        } catch (RuntimeException e) {
            throw new IntegrasjonException("FP-824246",
                "Feil ved kall til inntektstjenesten. Meld til #team_registre og #produksjonshendelser hvis dette skjer over lengre tidsperiode.", e);
        }
        return response;
    }

    private RestRequest lagRequest(FinnInntektRequest finnInntektRequest, InntektskildeType kilde) {
        var request = new HentInntektListeBolkRequest();

        if (finnInntektRequest.getFnr() != null) {
            request.setIdentListe(Collections.singletonList(Aktoer.newNaturligIdent(finnInntektRequest.getFnr())));
        } else {
            request.setIdentListe(Collections.singletonList(Aktoer.newAktoerId(finnInntektRequest.getAktørId())));
        }

        InntektsFilter filter = getFilter(kilde);
        if (filter != null) {
            request.setAinntektsfilter(filter.getKode());
            request.setFormaal(filter.getFormål().getKode());
        }
        request.setMaanedFom(finnInntektRequest.getFom().isAfter(INNTK_TIDLIGSTE_DATO) ? finnInntektRequest.getFom() : INNTK_TIDLIGSTE_DATO);
        request.setMaanedTom(finnInntektRequest.getTom().isAfter(INNTK_TIDLIGSTE_DATO) ? finnInntektRequest.getTom() : INNTK_TIDLIGSTE_DATO);
        return RestRequest.newPOSTJson(request, endpoint, InntektTjeneste.class);
    }

    private InntektsFilter getFilter(InntektskildeType kilde) {
        // Skal bare få en verdi.
        return kildeTilFilter.getOrDefault(kilde, null);
    }

    private InntektsInformasjon oversettResponse(HentInntektListeBolkResponse response, InntektskildeType kilde, YtelseType ytelse) {
        if (response.getSikkerhetsavvikListe() != null && !response.getSikkerhetsavvikListe().isEmpty()) {
            throw new IntegrasjonException("FP-535194",
                String.format("Fikk følgende sikkerhetsavvik ved kall til inntektstjenesten: %s.", byggSikkerhetsavvikString(response)));
        }

        List<Månedsinntekt> månedsinntekter = new ArrayList<>();
        List<FrilansArbeidsforhold> arbeidsforhold = new ArrayList<>();

        List<ArbeidsInntektIdent> arbeidsInntektIdentListe = response.getArbeidsInntektIdentListe();
        if (response.getArbeidsInntektIdentListe() != null) {
            for (var arbeidsInntektIdent : arbeidsInntektIdentListe) {
                if (arbeidsInntektIdent.getArbeidsInntektMaaned() != null) {
                    for (ArbeidsInntektMaaned arbeidsInntektMaaned : arbeidsInntektIdent.getArbeidsInntektMaaned()) {
                        ArbeidsInntektInformasjon arbeidsInntektInformasjon = oversettInntekter(månedsinntekter, arbeidsInntektMaaned, kilde);
                        if (YtelseType.FRISINN.equals(ytelse)) {
                            oversettArbeidsforhold(arbeidsforhold, arbeidsInntektInformasjon, ytelse);
                        }
                    }
                }
            }
        }
        return new InntektsInformasjon(månedsinntekter, arbeidsforhold, kilde);
    }

    private ArbeidsInntektInformasjon oversettInntekter(List<Månedsinntekt> månedsinntekter, ArbeidsInntektMaaned arbeidsInntektMaaned, InntektskildeType kilde) {
        var arbeidsInntektInformasjon = arbeidsInntektMaaned.getArbeidsInntektInformasjon();

        if (arbeidsInntektInformasjon != null && arbeidsInntektInformasjon.getInntektListe() != null) {
            for (var inntekt : arbeidsInntektInformasjon.getInntektListe()) {
                var brukYM = inntekt.getUtbetaltIMaaned();
                var tilleggsinformasjon = inntekt.getTilleggsinformasjon();
                if (erYtelseFraOffentlig(inntekt)
                    && erEtterbetaling(tilleggsinformasjon)
                    && skalPeriodisereInntektsKilde(kilde)) {
                    brukYM = YearMonth.from(((Etterbetalingsperiode) tilleggsinformasjon.getTilleggsinformasjonDetaljer())
                        .getEtterbetalingsperiodeFom().plusDays(1));
                }
                var månedsinntekt = new Månedsinntekt.Builder()
                    .medBeløp(inntekt.getBeloep())
                    .medSkatteOgAvgiftsregelType(inntekt.getSkatteOgAvgiftsregel());

                if (brukYM != null) {
                    månedsinntekt.medMåned(brukYM);
                }
                utledOgSettUtbetalerOgYtelse(inntekt, månedsinntekt);

                månedsinntekter.add(månedsinntekt.build());
            }
        }
        return arbeidsInntektInformasjon;
    }

    private boolean skalPeriodisereInntektsKilde(InntektskildeType kilde) {
        return SKAL_PERIODISERE_INNTEKTSKILDE.contains(kilde);
    }

    private boolean erEtterbetaling(Tilleggsinformasjon tilleggsinformasjon) {
        return tilleggsinformasjon != null &&
            TilleggsinformasjonDetaljerType.ETTERBETALINGSPERIODE
                .equals(tilleggsinformasjon.getTilleggsinformasjonDetaljer().getDetaljerType());
    }

    private void oversettArbeidsforhold(List<FrilansArbeidsforhold> arbeidsforhold, ArbeidsInntektInformasjon arbeidsInntektInformasjon, YtelseType ytelse) {
        if (arbeidsInntektInformasjon.getArbeidsforholdListe() == null) {
            return;
        }
        for (var arbeidsforholdFrilanser : arbeidsInntektInformasjon.getArbeidsforholdListe()) {
            var builder = FrilansArbeidsforhold.builder();
            var arbeidType = ArbeidType.finnForKodeverkEiersKode(arbeidsforholdFrilanser.getArbeidsforholdstype());
            builder.medArbeidsforholdId(arbeidsforholdFrilanser.getArbeidsforholdID())
                .medType(arbeidType) // OK med NPE
                .medSisteEndringIStillingsprosent(arbeidsforholdFrilanser.getSisteDatoForStillingsprosentendring())
                .medSisteEndringILønn(arbeidsforholdFrilanser.getSisteLoennsendring())
                .medStillingsprosent(BigDecimal.valueOf(arbeidsforholdFrilanser.getStillingsprosent()))
                .medFom(arbeidsforholdFrilanser.getFrilansPeriodeFom())
                .medTom(arbeidsforholdFrilanser.getFrilansPeriodeTom());

            if (arbeidsforholdFrilanser.getAntallTimerPerUkeSomEnFullStillingTilsvarer() != null) {
                builder.medBeregnetAntallTimerPerUke(BigDecimal.valueOf(arbeidsforholdFrilanser.getAntallTimerPerUkeSomEnFullStillingTilsvarer()));
            }
            oversettArbeidsgiver(arbeidsforholdFrilanser, builder, ytelse);

            arbeidsforhold.add(builder.build());
        }
    }

    private void oversettArbeidsgiver(ArbeidsforholdFrilanser arbeidsforholdFrilanser, FrilansArbeidsforhold.Builder builder, YtelseType ytelse) {
        var arbeidsgiver = arbeidsforholdFrilanser.getArbeidsgiver();
        if (AktoerType.AKTOER_ID.equals(arbeidsgiver.getAktoerType())) { // OK med NPE hvis arbeidsgiver er null
            builder.medArbeidsgiverAktørId(new AktørId(arbeidsgiver.getIdentifikator()));
        } else if (AktoerType.ORGANISASJON.equals(arbeidsgiver.getAktoerType())) {
            builder.medArbeidsgiverOrgnr(arbeidsgiver.getIdentifikator());
        } else if (AktoerType.NATURLIG_IDENT.equals(arbeidsgiver.getAktoerType())) {
            AktørId aktørId = aktørConsumer.hentAktørForIdent(new PersonIdent(arbeidsgiver.getIdentifikator()), ytelse).orElse(null);
            builder.medArbeidsgiverAktørId(aktørId);
        } else {
            logger.info("Arbeidsgiver for frilanser har ukjent aktørtype: {}", arbeidsgiver.getAktoerType());
        }
    }

    private void utledOgSettUtbetalerOgYtelse(Inntekt inntekt, Månedsinntekt.Builder månedsinntekt) {
        if (erYtelseFraOffentlig(inntekt)) {
            månedsinntekt.medYtelse(true)
                .medYtelseKode(inntekt.getBeskrivelse());
        } else if (erPensjonEllerTrygd(inntekt)) {
            månedsinntekt.medYtelse(true)
                .medPensjonEllerTrygdKode(inntekt.getBeskrivelse());
        } else if (erNæringsinntekt(inntekt)) {
            månedsinntekt.medYtelse(true)
                .medNæringsinntektKode(inntekt.getBeskrivelse());
        } else if (erLønn(inntekt)) {
            månedsinntekt.medYtelse(false);
            månedsinntekt.medArbeidsgiver(inntekt.getVirksomhet().getIdentifikator()); // OK med NPE hvis inntekt.getArbeidsgiver() er null
            månedsinntekt.medArbeidsforholdRef(inntekt.getArbeidsforholdREF());
        } else {
            throw new TekniskException("FP-711674",
                String.format("Kunne ikke mappe svar fra Inntektskomponenten: virksomhet=%s, inntektType=%s",
                inntekt.getVirksomhet().getIdentifikator(), inntekt.getInntektType()));
        }
    }

    private boolean erLønn(Inntekt inntekt) {
        return InntektType.LOENNSINNTEKT.equals(inntekt.getInntektType());
    }

    private boolean erYtelseFraOffentlig(Inntekt inntekt) {
        return InntektType.YTELSE_FRA_OFFENTLIGE.equals(inntekt.getInntektType());
    }

    private boolean erPensjonEllerTrygd(Inntekt inntekt) {
        return InntektType.PENSJON_ELLER_TRYGD.equals(inntekt.getInntektType());
    }

    private boolean erNæringsinntekt(Inntekt inntekt) {
        return InntektType.NAERINGSINNTEKT.equals(inntekt.getInntektType());
    }

    private String byggSikkerhetsavvikString(HentInntektListeBolkResponse response) {
        var stringBuilder = new StringBuilder();
        var sikkerhetsavvikListe = response.getSikkerhetsavvikListe();
        if (sikkerhetsavvikListe != null && !sikkerhetsavvikListe.isEmpty()) {
            stringBuilder.append(sikkerhetsavvikListe.get(0).getTekst());
            for (int i = 1; i < sikkerhetsavvikListe.size(); i++) {
                stringBuilder.append(", ");
                stringBuilder.append(sikkerhetsavvikListe.get(i).getTekst());
            }
        }
        return stringBuilder.toString();
    }
}
