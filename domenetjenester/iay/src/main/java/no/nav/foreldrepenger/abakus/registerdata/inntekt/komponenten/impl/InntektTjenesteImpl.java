package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl;

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

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsFilter;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsFormål;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.FinnInntektRequest;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.FrilansArbeidsforhold;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.InntektTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.InntektsInformasjon;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.Månedsinntekt;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.tjenester.aordningen.inntektsinformasjon.Aktoer;
import no.nav.tjenester.aordningen.inntektsinformasjon.AktoerType;
import no.nav.tjenester.aordningen.inntektsinformasjon.ArbeidsInntektIdent;
import no.nav.tjenester.aordningen.inntektsinformasjon.ArbeidsInntektInformasjon;
import no.nav.tjenester.aordningen.inntektsinformasjon.ArbeidsInntektMaaned;
import no.nav.tjenester.aordningen.inntektsinformasjon.ArbeidsforholdFrilanser;
import no.nav.tjenester.aordningen.inntektsinformasjon.Sikkerhetsavvik;
import no.nav.tjenester.aordningen.inntektsinformasjon.inntekt.Inntekt;
import no.nav.tjenester.aordningen.inntektsinformasjon.inntekt.InntektType;
import no.nav.tjenester.aordningen.inntektsinformasjon.request.HentInntektListeBolkRequest;
import no.nav.tjenester.aordningen.inntektsinformasjon.response.HentInntektListeBolkResponse;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class InntektTjenesteImpl implements InntektTjeneste {

    private static final String ENDPOINT_KEY = "hentinntektlistebolk.url";
    private static final String INNTK_DATO_KEY = "inntektskomponent.tidligste.dato";
    private static final Logger logger = LoggerFactory.getLogger(InntektTjenesteImpl.class);

    private OidcRestClient oidcRestClient;
    private URI endpoint;
    private YearMonth requestTillattFom;
    private KodeverkRepository kodeverkRepository;
    private AktørConsumer aktørConsumer;
    private Map<InntektsKilde, Set<InntektsFilter>> kildeTilFilter;
    private Map<InntektsFilter, Set<InntektsFormål>> filterTilFormål;

    InntektTjenesteImpl() {
        // For CDI proxy
    }

    @Inject
    public InntektTjenesteImpl(@KonfigVerdi(ENDPOINT_KEY) URI endpoint,
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

    @Override
    public InntektsInformasjon finnInntekt(FinnInntektRequest finnInntektRequest, InntektsKilde kilde) {
        HentInntektListeBolkRequest request = lagRequest(finnInntektRequest, kilde);

        HentInntektListeBolkResponse response;
        try {
            response = oidcRestClient.post(endpoint, request, HentInntektListeBolkResponse.class);
        } catch (RuntimeException e) {
            throw InntektFeil.FACTORY.feilVedKallTilInntekt(e).toException();
        }
        return oversettResponse(response, kilde);
    }

    private HentInntektListeBolkRequest lagRequest(FinnInntektRequest finnInntektRequest, InntektsKilde kilde) {
        HentInntektListeBolkRequest request = new HentInntektListeBolkRequest();

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
        for (ArbeidsInntektIdent arbeidsInntektIdent : arbeidsInntektIdentListe) {
            for (ArbeidsInntektMaaned arbeidsInntektMaaned : arbeidsInntektIdent.getArbeidsInntektMaaned()) {
                ArbeidsInntektInformasjon arbeidsInntektInformasjon = oversettInntekter(månedsinntekter, arbeidsInntektMaaned);
                oversettArbeidsforhold(arbeidsforhold, arbeidsInntektInformasjon);
            }
        }
        return new InntektsInformasjon(månedsinntekter, arbeidsforhold, kilde);
    }

    private ArbeidsInntektInformasjon oversettInntekter(List<Månedsinntekt> månedsinntekter, ArbeidsInntektMaaned arbeidsInntektMaaned) {
        ArbeidsInntektInformasjon arbeidsInntektInformasjon = arbeidsInntektMaaned.getArbeidsInntektInformasjon();

        for (Inntekt inntekt : arbeidsInntektInformasjon.getInntektListe()) {
            Månedsinntekt.Builder månedsinntekt = new Månedsinntekt.Builder()
                .medBeløp(inntekt.getBeloep())
                .medSkatteOgAvgiftsregelType(inntekt.getSkatteOgAvgiftsregel());

            if (inntekt.getUtbetaltIMaaned() != null) {
                månedsinntekt.medMåned(inntekt.getUtbetaltIMaaned());
            }
            utledOgSettUtbetalerOgYtelse(inntekt, månedsinntekt);

            månedsinntekter.add(månedsinntekt.build());
        }
        return arbeidsInntektInformasjon;
    }

    private void oversettArbeidsforhold(List<FrilansArbeidsforhold> arbeidsforhold, ArbeidsInntektInformasjon arbeidsInntektInformasjon) {
        if (arbeidsInntektInformasjon.getArbeidsforholdListe() == null) {
            return;
        }
        for (ArbeidsforholdFrilanser arbeidsforholdFrilanser : arbeidsInntektInformasjon.getArbeidsforholdListe()) {
            FrilansArbeidsforhold.Builder builder = FrilansArbeidsforhold.builder();
            builder.medArbeidsforholdId(arbeidsforholdFrilanser.getArbeidsforholdID())
                .medType(kodeverkRepository.finnForKodeverkEiersKode(ArbeidType.class, arbeidsforholdFrilanser.getArbeidsforholdstype())) // OK med NPE
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
            AktørId aktørId = new AktørId(aktørConsumer.hentAktørIdForPersonIdent(arbeidsgiver.getIdentifikator()).orElse(null));
            builder.medArbeidsgiverAktørId(aktørId);
        } else {
            logger.info("ArbeidsgiverEntitet for frilanser har ukjent aktørtype: {}", arbeidsgiver.getAktoerType());
        }
    }

    private void utledOgSettUtbetalerOgYtelse(Inntekt inntekt, Månedsinntekt.Builder månedsinntekt) {
        if (erYtelse(inntekt)) {
            månedsinntekt.medYtelse(true)
                .medYtelseKode(inntekt.getBeskrivelse());
            return;
        } else if (erPensjonEllerTrygd(inntekt)) {
            månedsinntekt.medYtelse(true)
                .medPensjonEllerTrygdKode(inntekt.getBeskrivelse());
            return;
        } else if (erNæringsinntekt(inntekt)) {
            månedsinntekt.medYtelse(true)
                .medNæringsinntektKode(inntekt.getBeskrivelse());
            return;
        } else if (erLønn(inntekt)) {
            månedsinntekt.medYtelse(false);
            månedsinntekt.medArbeidsgiver(inntekt.getVirksomhet().getIdentifikator()); // OK med NPE hvis inntekt.getArbeidsgiver() er null
            månedsinntekt.medArbeidsforholdRef(inntekt.getArbeidsforholdREF());
            return;
        }
        throw InntektFeil.FACTORY.kunneIkkeMappeResponse().toException();
    }

    private boolean erLønn(Inntekt inntekt) {
        return InntektType.LOENNSINNTEKT.equals(inntekt.getInntektType());
    }

    private boolean erYtelse(Inntekt inntekt) {
        return InntektType.YTELSE_FRA_OFFENTLIGE.equals(inntekt.getInntektType());
    }

    private boolean erPensjonEllerTrygd(Inntekt inntekt) {
        return InntektType.PENSJON_ELLER_TRYGD.equals(inntekt.getInntektType());
    }

    private boolean erNæringsinntekt(Inntekt inntekt) {
        return InntektType.NAERINGSINNTEKT.equals(inntekt.getInntektType());
    }

    private String byggSikkerhetsavvikString(HentInntektListeBolkResponse response) {
        StringBuilder stringBuilder = new StringBuilder();
        List<Sikkerhetsavvik> sikkerhetsavvikListe = response.getSikkerhetsavvikListe();
        if (!sikkerhetsavvikListe.isEmpty()) {
            stringBuilder.append(sikkerhetsavvikListe.get(0).getTekst());
            for (int i = 1; i < sikkerhetsavvikListe.size(); i++) {
                stringBuilder.append(", ");
                stringBuilder.append(sikkerhetsavvikListe.get(i).getTekst());
            }
        }
        return stringBuilder.toString();
    }
}
