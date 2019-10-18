package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsFilter;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsFormål;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.typer.AktørId;
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
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class InntektTjeneste {

    private static final String ENDPOINT_KEY = "hentinntektlistebolk.url";
    private static final String INNTK_DATO_KEY = "inntektskomponent.tidligste.dato";
    private static final Logger logger = LoggerFactory.getLogger(InntektTjeneste.class);
    private static final Set<InntektsKilde> SKAL_PERIODISERE_INNTEKTSKILDE = Set.of(InntektsKilde.INNTEKT_SAMMENLIGNING, InntektsKilde.INNTEKT_BEREGNING);

    private OidcRestClient oidcRestClient;
    private URI endpoint;
    private YearMonth requestTillattFom;
    private KodeverkRepository kodeverkRepository;
    private AktørConsumer aktørConsumer;
    private Map<InntektsKilde, Set<InntektsFilter>> kildeTilFilter;
    private Map<InntektsFilter, Set<InntektsFormål>> filterTilFormål;

    InntektTjeneste() {
        // For CDI proxy
    }

    @Inject
    public InntektTjeneste(@KonfigVerdi(ENDPOINT_KEY) URI endpoint,
                           @KonfigVerdi(INNTK_DATO_KEY) String inntkDato,
                           OidcRestClient oidcRestClient,
                           KodeverkRepository kodeverkRepository,
                           AktørConsumer aktørConsumer) {
        this.endpoint = endpoint;
        this.requestTillattFom = YearMonth.from(LocalDate.parse(inntkDato, DateTimeFormatter.ISO_LOCAL_DATE));
        this.oidcRestClient = oidcRestClient;
        this.kodeverkRepository = kodeverkRepository;
        this.aktørConsumer = aktørConsumer;
        this.kildeTilFilter = kodeverkRepository.hentKodeRelasjonForKodeverk(InntektsKilde.class, InntektsFilter.class);
        this.filterTilFormål = kodeverkRepository.hentKodeRelasjonForKodeverk(InntektsFilter.class, InntektsFormål.class);
    }

    public InntektsInformasjon finnInntekt(FinnInntektRequest finnInntektRequest, InntektsKilde kilde) {
        var request = lagRequest(finnInntektRequest, kilde);

        HentInntektListeBolkResponse response;
        try {
            response = oidcRestClient.post(endpoint, request, HentInntektListeBolkResponse.class);
        } catch (RuntimeException e) {
            throw InntektFeil.FACTORY.feilVedKallTilInntekt(e).toException();
        }
        return oversettResponse(response, kilde);

    }

    private HentInntektListeBolkRequest lagRequest(FinnInntektRequest finnInntektRequest, InntektsKilde kilde) {
        var request = new HentInntektListeBolkRequest();

        if (finnInntektRequest.getFnr() != null) {
            request.setIdentListe(Collections.singletonList(Aktoer.newNaturligIdent(finnInntektRequest.getFnr())));
        } else {
            request.setIdentListe(Collections.singletonList(Aktoer.newAktoerId(finnInntektRequest.getAktørId())));
        }

        InntektsFilter filter = getFilter(kilde);
        request.setAinntektsfilter(filter.getOffisiellKode());
        request.setFormaal(getFormål(filter).getOffisiellKode());
        request.setMaanedFom(finnInntektRequest.getFom().isAfter(requestTillattFom) ? finnInntektRequest.getFom() : requestTillattFom);
        request.setMaanedTom(finnInntektRequest.getTom().isAfter(requestTillattFom) ? finnInntektRequest.getTom() : requestTillattFom);
        return request;
    }

    private InntektsFilter getFilter(InntektsKilde kilde) {
        // Skal bare få en verdi.
        return kildeTilFilter.getOrDefault(kilde, Collections.emptySet()).stream().findFirst().orElse(InntektsFilter.UDEFINERT);
    }

    private InntektsFormål getFormål(InntektsFilter filter) {
        // Skal bare få en verdi.
        return filterTilFormål.getOrDefault(filter, Collections.emptySet()).stream().findFirst().orElse(InntektsFormål.UDEFINERT);
    }

    private InntektsInformasjon oversettResponse(HentInntektListeBolkResponse response, InntektsKilde kilde) {
        if (response.getSikkerhetsavvikListe() != null && !response.getSikkerhetsavvikListe().isEmpty()) {
            throw InntektFeil.FACTORY.fikkSikkerhetsavvikFraInntekt(byggSikkerhetsavvikString(response)).toException();
        }

        List<Månedsinntekt> månedsinntekter = new ArrayList<>();
        List<FrilansArbeidsforhold> arbeidsforhold = new ArrayList<>();

        List<ArbeidsInntektIdent> arbeidsInntektIdentListe = response.getArbeidsInntektIdentListe();
        if (response.getArbeidsInntektIdentListe() != null) {
            for (var arbeidsInntektIdent : arbeidsInntektIdentListe) {
                for (ArbeidsInntektMaaned arbeidsInntektMaaned : arbeidsInntektIdent.getArbeidsInntektMaaned()) {
                    ArbeidsInntektInformasjon arbeidsInntektInformasjon = oversettInntekter(månedsinntekter, arbeidsInntektMaaned, kilde);
                    oversettArbeidsforhold(arbeidsforhold, arbeidsInntektInformasjon);
                }
            }
        }
        return new InntektsInformasjon(månedsinntekter, arbeidsforhold, kilde);
    }

    private ArbeidsInntektInformasjon oversettInntekter(List<Månedsinntekt> månedsinntekter, ArbeidsInntektMaaned arbeidsInntektMaaned, InntektsKilde kilde) {
        var arbeidsInntektInformasjon = arbeidsInntektMaaned.getArbeidsInntektInformasjon();

        if (arbeidsInntektInformasjon != null && arbeidsInntektInformasjon.getInntektListe() != null) {
            for (var inntekt : arbeidsInntektInformasjon.getInntektListe()) {
                var brukYM = inntekt.getUtbetaltIMaaned();
                var tilleggsinformasjon = inntekt.getTilleggsinformasjon();
                if (erYtelseFraOffentlig(inntekt)
                    && erEtterbetaling(tilleggsinformasjon)
                    && skalPeriodisereInntektsKilde(kilde)) {
                    brukYM = YearMonth.from(
                        ((Etterbetalingsperiode) tilleggsinformasjon.getTilleggsinformasjonDetaljer()).getEtterbetalingsperiodeFom().plusDays(1));
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

    private boolean skalPeriodisereInntektsKilde(InntektsKilde kilde) {
        return SKAL_PERIODISERE_INNTEKTSKILDE.contains(kilde);
    }

    private boolean erEtterbetaling(Tilleggsinformasjon tilleggsinformasjon) {
        return tilleggsinformasjon != null &&
            TilleggsinformasjonDetaljerType.ETTERBETALINGSPERIODE
                .equals(tilleggsinformasjon.getTilleggsinformasjonDetaljer().getDetaljerType());
    }

    private void oversettArbeidsforhold(List<FrilansArbeidsforhold> arbeidsforhold, ArbeidsInntektInformasjon arbeidsInntektInformasjon) {
        if (arbeidsInntektInformasjon.getArbeidsforholdListe() == null) {
            return;
        }
        for (var arbeidsforholdFrilanser : arbeidsInntektInformasjon.getArbeidsforholdListe()) {
            var builder = FrilansArbeidsforhold.builder();
            var arbeidType = kodeverkRepository.finnForKodeverkEiersKode(ArbeidType.class, arbeidsforholdFrilanser.getArbeidsforholdstype());
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
            oversettArbeidsgiver(arbeidsforholdFrilanser, builder);

            arbeidsforhold.add(builder.build());
        }
    }

    private void oversettArbeidsgiver(ArbeidsforholdFrilanser arbeidsforholdFrilanser, FrilansArbeidsforhold.Builder builder) {
        var arbeidsgiver = arbeidsforholdFrilanser.getArbeidsgiver();
        if (AktoerType.AKTOER_ID.equals(arbeidsgiver.getAktoerType())) { // OK med NPE hvis arbeidsgiver er null
            builder.medArbeidsgiverAktørId(new AktørId(arbeidsgiver.getIdentifikator()));
        } else if (AktoerType.ORGANISASJON.equals(arbeidsgiver.getAktoerType())) {
            builder.medArbeidsgiverOrgnr(arbeidsgiver.getIdentifikator());
        } else if (AktoerType.NATURLIG_IDENT.equals(arbeidsgiver.getAktoerType())) {
            AktørId aktørId = aktørConsumer.hentAktørIdForPersonIdent(arbeidsgiver.getIdentifikator()).map(AktørId::new).orElse(null);
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
            throw InntektFeil.FACTORY.kunneIkkeMappeResponse(inntekt.getVirksomhet().getIdentifikator(), String.valueOf(inntekt.getInntektType()))
                .toException();
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
        if (sikkerhetsavvikListe!=null && !sikkerhetsavvikListe.isEmpty()) {
            stringBuilder.append(sikkerhetsavvikListe.get(0).getTekst());
            for (int i = 1; i < sikkerhetsavvikListe.size(); i++) {
                stringBuilder.append(", ");
                stringBuilder.append(sikkerhetsavvikListe.get(i).getTekst());
            }
        }
        return stringBuilder.toString();
    }
}
