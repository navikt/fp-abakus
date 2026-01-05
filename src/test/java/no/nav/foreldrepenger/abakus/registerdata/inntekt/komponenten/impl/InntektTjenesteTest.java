package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
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
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.InntektTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.Inntektsfilter;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.Inntektstype;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ExtendWith(MockitoExtension.class)
class InntektTjenesteTest {

    private static final String FNR = "01234567890";
    private static final YearMonth GJELDENDE_MÅNED = YearMonth.now();
    private static final String SYKEPENGER = "sykepenger";
    private static final String ORGNR = "456";
    URI uri = URI.create("http://dummy");

    @Mock
    private RestClient restKlient;
    private InntektTjeneste inntektTjeneste;

    @BeforeEach
    void before() {
        inntektTjeneste = new InntektTjeneste(restKlient, new RestConfig(TokenFlow.AZUREAD_CC, uri, null, null));
    }

    @Test
    void skal_kalle_consumer_og_oversette_response() {
        // Arrange
        var response = opprettResponse(Inntektsfilter.SAMMENLIGNINGSGRUNNLAG);
        var responseB = opprettResponse(Inntektsfilter.BEREGNINGSGRUNNLAG);

        when(restKlient.send(any(), any())).thenReturn(new InntektTjeneste.InntektBulkApiUt(List.of(response, responseB)));

        // Tre måneder siden
        var arbeidsInntektInformasjonMnd3 = opprettInntektsinformasjon(GJELDENDE_MÅNED.minusMonths(3));
        arbeidsInntektInformasjonMnd3.inntektListe().add(opprettInntekt(new BigDecimal(50), SYKEPENGER, Inntektstype.YTELSE, null));
        response.data().add(arbeidsInntektInformasjonMnd3);

        // To måneder siden
        var arbeidsInntektInformasjonMnd2a = opprettInntektsinformasjon(GJELDENDE_MÅNED.minusMonths(2));
        arbeidsInntektInformasjonMnd2a.inntektListe().add(opprettInntekt(new BigDecimal(100), SYKEPENGER, Inntektstype.YTELSE, null));
        response.data().add(arbeidsInntektInformasjonMnd2a);
        var arbeidsInntektInformasjonMnd2b = opprettInntektsinformasjon(GJELDENDE_MÅNED.minusMonths(2));
        arbeidsInntektInformasjonMnd2b.inntektListe().add(opprettInntekt(new BigDecimal(200), null, Inntektstype.LØNN, null));
        response.data().add(arbeidsInntektInformasjonMnd2b);

        // En måned siden
        var arbeidsInntektInformasjonMnd1 = opprettInntektsinformasjon(GJELDENDE_MÅNED.minusMonths(1));
        arbeidsInntektInformasjonMnd1.inntektListe().add(opprettInntekt(new BigDecimal(400), null, Inntektstype.LØNN, null));
        response.data().add(arbeidsInntektInformasjonMnd1);

        // Denne måneden
        var arbeidsInntektInformasjonMnd0 = opprettInntektsinformasjon(GJELDENDE_MÅNED);
        arbeidsInntektInformasjonMnd0.inntektListe().add(opprettInntekt(new BigDecimal(405), null, Inntektstype.LØNN, null));
        response.data().add(arbeidsInntektInformasjonMnd0);


        // Act
        var inntektsInformasjon = inntektTjeneste.finnInntekt(FNR, GJELDENDE_MÅNED.minusMonths(3), GJELDENDE_MÅNED, Set.of(InntektskildeType.INNTEKT_SAMMENLIGNING, InntektskildeType.INNTEKT_BEREGNING));

        // Assert
        verify(restKlient, times(1)).send(any(), any());

        assertThat(inntektsInformasjon.keySet()).hasSize(2);
        final var månedsinntekter = inntektsInformasjon.get(InntektskildeType.INNTEKT_SAMMENLIGNING).månedsinntekter();
        assertThat(månedsinntekter).hasSize(5);
        assertThat(månedsinntekter.getFirst().beløp()).isEqualTo(new BigDecimal(50));
        assertThat(månedsinntekter.get(0).måned()).isEqualTo(GJELDENDE_MÅNED.minusMonths(3));
        assertThat(månedsinntekter.get(0).isYtelse()).isTrue();
        assertThat(månedsinntekter.get(2).beløp()).isEqualTo(new BigDecimal(200));
        assertThat(månedsinntekter.get(2).måned()).isEqualTo(GJELDENDE_MÅNED.minusMonths(2));
        assertThat(månedsinntekter.get(2).arbeidsgiver()).isEqualTo(ORGNR);
        assertThat(månedsinntekter.get(2).isYtelse()).isFalse();
    }

    @Test
    void splitter_inntekt_per_måned_dersom_gjelder_flere_måneder_og_er_inntektstype_ytelse_og_etterbetaling() {
        // Arrange
        var response = opprettResponse(Inntektsfilter.SAMMENLIGNINGSGRUNNLAG);
        var fraDato = LocalDate.of(2025, 3, 28);
        var tildato = LocalDate.of(2025, 5, 25);
        var månedFra = YearMonth.from(fraDato);
        var månedTil = YearMonth.from(tildato);

        when(restKlient.send(any(), any())).thenReturn(new InntektTjeneste.InntektBulkApiUt(List.of(response)));

        // Denne måneden
        var arbeidsInntektInformasjonMnd0 = opprettInntektsinformasjon(månedFra);
        arbeidsInntektInformasjonMnd0.inntektListe().add(opprettInntekt(new BigDecimal(100000), "beskrivelse", Inntektstype.YTELSE, new InntektTjeneste.Tilleggsinformasjon("Etterbetalingsperiode", fraDato, tildato)));
        response.data().add(arbeidsInntektInformasjonMnd0);

        // Act
        var inntektsInformasjon = inntektTjeneste.finnInntekt(FNR, månedFra, månedTil, Set.of(InntektskildeType.INNTEKT_SAMMENLIGNING, InntektskildeType.INNTEKT_BEREGNING));

        // Assert
        verify(restKlient, times(1)).send(any(), any());

        assertThat(inntektsInformasjon.keySet()).hasSize(1);
        final var månedsinntekter = inntektsInformasjon.get(InntektskildeType.INNTEKT_SAMMENLIGNING).månedsinntekter();
        assertThat(månedsinntekter).hasSize(3);
        assertThat(månedsinntekter.getFirst().måned()).isEqualTo(månedFra);
        assertThat(månedsinntekter.getFirst().beløp()).isEqualTo(new BigDecimal("2500.0000000000"));
        assertThat(månedsinntekter.getFirst().arbeidsgiver()).isNull();
        assertThat(månedsinntekter.getFirst().isYtelse()).isTrue();
        assertThat(månedsinntekter.get(1).måned()).isEqualTo(månedFra.plusMonths(1));
        assertThat(månedsinntekter.get(1).beløp()).isEqualTo(new BigDecimal("55000.0000000000"));
        assertThat(månedsinntekter.get(1).arbeidsgiver()).isNull();
        assertThat(månedsinntekter.get(1).isYtelse()).isTrue();
        assertThat(månedsinntekter.get(2).måned()).isEqualTo(månedTil);
        assertThat(månedsinntekter.get(2).beløp()).isEqualTo(new BigDecimal("42500.0000000000"));
        assertThat(månedsinntekter.get(2).arbeidsgiver()).isNull();
        assertThat(månedsinntekter.get(2).isYtelse()).isTrue();
    }

    private InntektTjeneste.InntektBulk opprettResponse(Inntektsfilter filter) {
        return new InntektTjeneste.InntektBulk(filter, new ArrayList<>());
    }

    private InntektTjeneste.Inntektsinformasjon opprettInntektsinformasjon(YearMonth måned) {
        return new InntektTjeneste.Inntektsinformasjon(måned, null, ORGNR, new ArrayList<>());
    }

    private InntektTjeneste.Inntekt opprettInntekt(BigDecimal beløp, String beskrivelse, Inntektstype inntektType,
                                                   InntektTjeneste.Tilleggsinformasjon tilleggsinformasjon) {
        return new InntektTjeneste.Inntekt(inntektType, beløp, beskrivelse, null, tilleggsinformasjon);
    }
}
