package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.finn.unleash.FakeUnleash;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.FinnInntektRequest;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.InntektTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.InntektsInformasjon;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.Månedsinntekt;
import no.nav.tjenester.aordningen.inntektsinformasjon.Aktoer;
import no.nav.tjenester.aordningen.inntektsinformasjon.AktoerType;
import no.nav.tjenester.aordningen.inntektsinformasjon.ArbeidsInntektIdent;
import no.nav.tjenester.aordningen.inntektsinformasjon.ArbeidsInntektInformasjon;
import no.nav.tjenester.aordningen.inntektsinformasjon.ArbeidsInntektMaaned;
import no.nav.tjenester.aordningen.inntektsinformasjon.Sikkerhetsavvik;
import no.nav.tjenester.aordningen.inntektsinformasjon.inntekt.Inntekt;
import no.nav.tjenester.aordningen.inntektsinformasjon.inntekt.InntektType;
import no.nav.tjenester.aordningen.inntektsinformasjon.request.HentInntektListeBolkRequest;
import no.nav.tjenester.aordningen.inntektsinformasjon.response.HentInntektListeBolkResponse;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

public class InntektTjenesteImplTest {
    private static final String FNR = "01234567890";
    private static final YearMonth GJELDENDE_MÅNED = YearMonth.now();
    private static final String SYKEPENGER = "sykepenger";
    private static final String ORGNR = "456";
    private static final String SIKKERHETSAVVIK1 = "Mangler rettighet 1";
    private static final String SIKKERHETSAVVIK2 = "Mangler rettighet 2";
    private URI uri = null;

    private OidcRestClient restKlient = mock(OidcRestClient.class);
    private KodeverkRepository kodeverkRepository = mock(KodeverkRepository.class);
    private InntektTjeneste inntektTjeneste;

    @Before
    public void before() {
        inntektTjeneste = new InntektTjeneste(uri, restKlient, kodeverkRepository, null, new FakeUnleash());
    }

    @Test
    public void skal_kalle_consumer_og_oversette_response() throws Exception {
        // Arrange
        HentInntektListeBolkResponse response = opprettResponse();

        when(restKlient.post(any(), any(), any())).thenReturn(response);

        Aktoer arbeidsplassen = new Aktoer();
        arbeidsplassen.setAktoerType(AktoerType.ORGANISASJON);
        arbeidsplassen.setIdentifikator(ORGNR);

        // Tre måneder siden
        ArbeidsInntektInformasjon arbeidsInntektInformasjonMnd1 = new ArbeidsInntektInformasjon();
        arbeidsInntektInformasjonMnd1.setInntektListe(Collections.singletonList(
            opprettInntekt(new BigDecimal(50), GJELDENDE_MÅNED.minusMonths(3), null, SYKEPENGER, InntektType.YTELSE_FRA_OFFENTLIGE)));
        ArbeidsInntektMaaned arbeidsInntektMaaned1 = new ArbeidsInntektMaaned();
        arbeidsInntektMaaned1.setArbeidsInntektInformasjon(arbeidsInntektInformasjonMnd1);
        response.getArbeidsInntektIdentListe().get(0).getArbeidsInntektMaaned().add(arbeidsInntektMaaned1);

        // To måneder siden
        ArbeidsInntektInformasjon arbeidsInntektInformasjonMnd2 = new ArbeidsInntektInformasjon();
        arbeidsInntektInformasjonMnd2.setInntektListe(new ArrayList<>());
        arbeidsInntektInformasjonMnd2.getInntektListe().add(
            opprettInntekt(new BigDecimal(100), GJELDENDE_MÅNED.minusMonths(2), null, SYKEPENGER, InntektType.YTELSE_FRA_OFFENTLIGE));
        arbeidsInntektInformasjonMnd2.getInntektListe().add(
            opprettInntekt(new BigDecimal(200), GJELDENDE_MÅNED.minusMonths(2), arbeidsplassen, null, InntektType.LOENNSINNTEKT));
        ArbeidsInntektMaaned arbeidsInntektMaaned2 = new ArbeidsInntektMaaned();
        arbeidsInntektMaaned2.setArbeidsInntektInformasjon(arbeidsInntektInformasjonMnd2);
        response.getArbeidsInntektIdentListe().get(0).getArbeidsInntektMaaned().add(arbeidsInntektMaaned2);

        // En måned siden
        ArbeidsInntektInformasjon arbeidsInntektInformasjonMnd3 = new ArbeidsInntektInformasjon();
        arbeidsInntektInformasjonMnd3.setInntektListe(Collections.singletonList(
            opprettInntekt(new BigDecimal(400), GJELDENDE_MÅNED.minusMonths(1), arbeidsplassen, null, InntektType.LOENNSINNTEKT)));
        ArbeidsInntektMaaned arbeidsInntektMaaned3 = new ArbeidsInntektMaaned();
        arbeidsInntektMaaned3.setArbeidsInntektInformasjon(arbeidsInntektInformasjonMnd3);
        response.getArbeidsInntektIdentListe().get(0).getArbeidsInntektMaaned().add(arbeidsInntektMaaned3);

        // Denne måneden
        ArbeidsInntektInformasjon arbeidsInntektInformasjonMnd4 = new ArbeidsInntektInformasjon();
        arbeidsInntektInformasjonMnd4.setInntektListe(Collections.singletonList(
            opprettInntekt(new BigDecimal(405), GJELDENDE_MÅNED, arbeidsplassen, null, InntektType.LOENNSINNTEKT)));
        ArbeidsInntektMaaned arbeidsInntektMaaned4 = new ArbeidsInntektMaaned();
        arbeidsInntektMaaned4.setArbeidsInntektInformasjon(arbeidsInntektInformasjonMnd4);
        response.getArbeidsInntektIdentListe().get(0).getArbeidsInntektMaaned().add(arbeidsInntektMaaned4);

        FinnInntektRequest finnInntektRequest = FinnInntektRequest
            .builder(GJELDENDE_MÅNED.minusMonths(3), GJELDENDE_MÅNED)
            .medFnr(FNR).build();

        // Act
        InntektsInformasjon inntektsInformasjon = inntektTjeneste.finnInntekt(finnInntektRequest, InntektsKilde.INNTEKT_OPPTJENING);

        // Assert
        ArgumentCaptor<HentInntektListeBolkRequest> requestCaptor = ArgumentCaptor.forClass(HentInntektListeBolkRequest.class);
        verify(restKlient, times(1)).post(eq(uri), requestCaptor.capture(), eq(HentInntektListeBolkResponse.class));

        HentInntektListeBolkRequest request = requestCaptor.getValue();
        assertThat(request.getIdentListe().size()).isEqualTo(1);
        assertThat(request.getIdentListe().get(0).getAktoerType().name()).isEqualTo(AktoerType.NATURLIG_IDENT.name());
        assertThat(request.getIdentListe().get(0).getIdentifikator()).isEqualTo(FNR);
        assertThat(request.getMaanedFom()).isEqualTo(GJELDENDE_MÅNED.minusMonths(3));
        assertThat(request.getMaanedTom()).isEqualTo(GJELDENDE_MÅNED);

        final List<Månedsinntekt> månedsinntekter = inntektsInformasjon.getMånedsinntekter();
        assertThat(månedsinntekter.size()).isEqualTo(5);
        assertThat(månedsinntekter.get(0).getBeløp()).isEqualTo(new BigDecimal(50));
        assertThat(månedsinntekter.get(0).getMåned()).isEqualTo(GJELDENDE_MÅNED.minusMonths(3));
        assertThat(månedsinntekter.get(0).isYtelse()).isTrue();
        assertThat(månedsinntekter.get(2).getBeløp()).isEqualTo(new BigDecimal(200));
        assertThat(månedsinntekter.get(2).getMåned()).isEqualTo(GJELDENDE_MÅNED.minusMonths(2));
        assertThat(månedsinntekter.get(2).getArbeidsgiver()).isEqualTo(ORGNR);
        assertThat(månedsinntekter.get(2).isYtelse()).isFalse();
    }

    @Test
    public void skal_avkorte_periode_gi_tom_response() throws Exception {
        // Arrange
        HentInntektListeBolkResponse response = opprettResponse();

        when(restKlient.post(any(), any(), any())).thenReturn(response);

        Aktoer arbeidsplassen = new Aktoer();
        arbeidsplassen.setAktoerType(AktoerType.ORGANISASJON);
        arbeidsplassen.setIdentifikator(ORGNR);
        YearMonth tidligst = YearMonth.from(LocalDate.parse("2015-07-01", DateTimeFormatter.ISO_LOCAL_DATE));

        FinnInntektRequest finnInntektRequest = FinnInntektRequest
            .builder(tidligst.minusMonths(3), tidligst.plusMonths(3))
            .medFnr(FNR).build();

        // Act
        final InntektsInformasjon inntektsInformasjon = inntektTjeneste.finnInntekt(finnInntektRequest, InntektsKilde.INNTEKT_OPPTJENING);

        // Assert
        ArgumentCaptor<HentInntektListeBolkRequest> requestCaptor = ArgumentCaptor.forClass(HentInntektListeBolkRequest.class);
        verify(restKlient, times(1)).post(eq(uri), requestCaptor.capture(), eq(HentInntektListeBolkResponse.class));

        HentInntektListeBolkRequest request = requestCaptor.getValue();
        assertThat(request.getIdentListe().size()).isEqualTo(1);
        assertThat(request.getIdentListe().get(0).getAktoerType().name()).isEqualTo(AktoerType.NATURLIG_IDENT.name());
        assertThat(request.getIdentListe().get(0).getIdentifikator()).isEqualTo(FNR);
        assertThat(request.getMaanedFom()).isEqualTo(tidligst);
        assertThat(request.getMaanedTom()).isEqualTo(tidligst.plusMonths(3));

        List<Månedsinntekt> månedsinntekter = inntektsInformasjon.getMånedsinntekter();
        assertThat(månedsinntekter.size()).isEqualTo(0);
    }

    @Test
    public void skal_oppdage_sikkerhetsavvik_i_response_og_kaste_exception() throws Exception {
        // Arrange
        HentInntektListeBolkResponse response = opprettResponse();
        Sikkerhetsavvik sikkerhetsavvik1 = new Sikkerhetsavvik();
        sikkerhetsavvik1.setTekst(SIKKERHETSAVVIK1);
        response.getSikkerhetsavvikListe().add(sikkerhetsavvik1);
        Sikkerhetsavvik sikkerhetsavvik2 = new Sikkerhetsavvik();
        sikkerhetsavvik2.setTekst(SIKKERHETSAVVIK2);
        response.getSikkerhetsavvikListe().add(sikkerhetsavvik2);
        when(restKlient.post(any(), any(), any())).thenReturn(response);

        FinnInntektRequest finnInntektRequest = FinnInntektRequest
            .builder(GJELDENDE_MÅNED.minusMonths(3), GJELDENDE_MÅNED)
            .medFnr(FNR).build();

        try {
            // Act
            inntektTjeneste.finnInntekt(finnInntektRequest, InntektsKilde.INNTEKT_OPPTJENING);
            fail("Forventet VLException");
        } catch (VLException e) {
            // Assert
            assertThat(e.getMessage()).contains(SIKKERHETSAVVIK1 + ", " + SIKKERHETSAVVIK2);
        }
    }

    private HentInntektListeBolkResponse opprettResponse() {
        HentInntektListeBolkResponse response = new HentInntektListeBolkResponse();
        response.setSikkerhetsavvikListe(new ArrayList<>());
        ArbeidsInntektIdent arbeidsInntektIdent = new ArbeidsInntektIdent();
        arbeidsInntektIdent.setArbeidsInntektMaaned(new ArrayList<>());

        response.setArbeidsInntektIdentListe(Collections.singletonList(arbeidsInntektIdent));
        return response;
    }

    private Inntekt opprettInntekt(BigDecimal beløp, YearMonth måned, Aktoer virksomhet, String beskrivelse, InntektType inntektType) {
        var inntekt = new Inntekt();
        inntekt.setInntektType(inntektType);
        inntekt.setBeloep(beløp);
        inntekt.setUtbetaltIMaaned(måned);
        inntekt.setVirksomhet(virksomhet);
        inntekt.setBeskrivelse(beskrivelse);
        return inntekt;
    }

}
