package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.net.URI;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.FinnInntektRequest;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.InntektV2Tjeneste;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.InntektsFilter;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ExtendWith(MockitoExtension.class)
class InntektV2TjenesteTest {

    private static final String FNR = "01234567890";
    private static final YearMonth GJELDENDE_MÅNED = YearMonth.now();
    private static final String SYKEPENGER = "sykepenger";
    private static final String ORGNR = "456";
    URI uri = URI.create("http://dummy");

    @Mock
    private RestClient restKlient;
    private InntektV2Tjeneste inntektTjeneste;

    @BeforeEach
    void before() {
        inntektTjeneste = new InntektV2Tjeneste(restKlient, new RestConfig(TokenFlow.AZUREAD_CC, uri, null, null));
    }

    @Test
    void skal_kalle_consumer_og_oversette_response() {
        // Arrange
        var response = opprettResponse(InntektsFilter.SAMMENLIGNINGSGRUNNLAG);
        var responseB = opprettResponse(InntektsFilter.BEREGNINGSGRUNNLAG);

        when(restKlient.send(any(), any())).thenReturn(new InntektV2Tjeneste.InntektBulkApiUt(List.of(response, responseB)));

        // Tre måneder siden
        var arbeidsInntektInformasjonMnd3 = opprettInntektsinformasjon(GJELDENDE_MÅNED.minusMonths(3), ORGNR);
        arbeidsInntektInformasjonMnd3.inntektListe().add(opprettInntekt(new BigDecimal(50), SYKEPENGER, "YtelseFraOffentlige"));
        response.data().add(arbeidsInntektInformasjonMnd3);

        // To måneder siden
        var arbeidsInntektInformasjonMnd2a = opprettInntektsinformasjon(GJELDENDE_MÅNED.minusMonths(2), ORGNR);
        arbeidsInntektInformasjonMnd2a.inntektListe().add(opprettInntekt(new BigDecimal(100), SYKEPENGER, "YtelseFraOffentlige"));
        response.data().add(arbeidsInntektInformasjonMnd2a);
        var arbeidsInntektInformasjonMnd2b = opprettInntektsinformasjon(GJELDENDE_MÅNED.minusMonths(2), ORGNR);
        arbeidsInntektInformasjonMnd2b.inntektListe().add(opprettInntekt(new BigDecimal(200), null, "Loennsinntekt"));
        response.data().add(arbeidsInntektInformasjonMnd2b);

        // En måned siden
        var arbeidsInntektInformasjonMnd1 = opprettInntektsinformasjon(GJELDENDE_MÅNED.minusMonths(1), ORGNR);
        arbeidsInntektInformasjonMnd1.inntektListe().add(opprettInntekt(new BigDecimal(400), null, "Loennsinntekt"));
        response.data().add(arbeidsInntektInformasjonMnd1);

        // Denne måneden
        var arbeidsInntektInformasjonMnd0 = opprettInntektsinformasjon(GJELDENDE_MÅNED, ORGNR);
        arbeidsInntektInformasjonMnd0.inntektListe().add(opprettInntekt(new BigDecimal(405), null, "Loennsinntekt"));
        response.data().add(arbeidsInntektInformasjonMnd0);


        var finnInntektRequest = FinnInntektRequest
                .builder(GJELDENDE_MÅNED.minusMonths(3), GJELDENDE_MÅNED)
                .medFnr(FNR).build();

        // Act
        var inntektsInformasjon = inntektTjeneste.finnInntekt(finnInntektRequest, Set.of(InntektskildeType.INNTEKT_SAMMENLIGNING, InntektskildeType.INNTEKT_BEREGNING));

        // Assert
        verify(restKlient, times(1)).send(any(), any());

        assertThat(inntektsInformasjon.keySet()).hasSize(2);
        final var månedsinntekter = inntektsInformasjon.get(InntektskildeType.INNTEKT_SAMMENLIGNING).getMånedsinntekter();
        assertThat(månedsinntekter).hasSize(5);
        assertThat(månedsinntekter.getFirst().getBeløp()).isEqualTo(new BigDecimal(50));
        assertThat(månedsinntekter.get(0).getMåned()).isEqualTo(GJELDENDE_MÅNED.minusMonths(3));
        assertThat(månedsinntekter.get(0).isYtelse()).isTrue();
        assertThat(månedsinntekter.get(2).getBeløp()).isEqualTo(new BigDecimal(200));
        assertThat(månedsinntekter.get(2).getMåned()).isEqualTo(GJELDENDE_MÅNED.minusMonths(2));
        assertThat(månedsinntekter.get(2).getArbeidsgiver()).isEqualTo(ORGNR);
        assertThat(månedsinntekter.get(2).isYtelse()).isFalse();
    }

    private InntektV2Tjeneste.InntektBulk opprettResponse(InntektsFilter filter) {
        return new InntektV2Tjeneste.InntektBulk(filter.getKode(), new ArrayList<>());
    }

    private InntektV2Tjeneste.Inntektsinformasjon opprettInntektsinformasjon(YearMonth måned, String underenhet) {
        return new InntektV2Tjeneste.Inntektsinformasjon(måned, null, underenhet, new ArrayList<>());
    }

    private InntektV2Tjeneste.Inntekt opprettInntekt(BigDecimal beløp, String beskrivelse, String inntektType) {
        return new InntektV2Tjeneste.Inntekt(inntektType, beløp, beskrivelse, null, null);
    }
}
