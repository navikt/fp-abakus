package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.time.MonthDay;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SSGResponse;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SigrunSummertSkattegrunnlagResponse;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class SigrunConsumerImplTest {

    private static final long AKTØR_ID = 123123L;
    String JSON_summerskattegrunnlag = """
        {
          "grunnlag": [
            {
              "tekniskNavn": "samledePaaloepteRenter",
              "beloep": 779981
            },
            {
              "tekniskNavn": "andreFradragsberettigedeKostnader",
              "beloep": 59981
            },
            {
              "tekniskNavn": "samletSkattepliktigOverskuddAvUtleieAvFritidseiendom",
              "beloep": 1609981
            },
            {
              "tekniskNavn": "skattepliktigAvkastningEllerKundeutbytte",
              "beloep": 1749981
            }
          ],
          "skatteoppgjoersdato": "2018-10-04",
          "svalbardGrunnlag": [
            {
              "tekniskNavn": "samledePaaloepteRenter",
              "beloep": 779981
            },
            {
              "tekniskNavn": "samletAndelAvInntektIBoligselskapEllerBoligsameie",
              "beloep": 849981
            },
            {
              "tekniskNavn": "loennsinntektMedTrygdeavgiftspliktOmfattetAvLoennstrekkordningen",
              "beloep": 1779981
            },
            {
              "tekniskNavn": "skattepliktigAvkastningEllerKundeutbytte",
              "beloep": 1749981
            }
          ]
        }
        """;
    private SigrunRestClient client = Mockito.mock(SigrunRestClient.class);
    private SigrunConsumer consumer = new SigrunConsumerImpl(client);
    private String JSON = """
        [
          {
            "tekniskNavn": "personinntektFiskeFangstFamiliebarnehage",
            "verdi": "814952"
          },
          {
            "tekniskNavn": "personinntektNaering",
            "verdi": "785896"
          },
          {
            "tekniskNavn": "personinntektBarePensjonsdel",
            "verdi": "844157"
          },
          {
            "tekniskNavn": "svalbardLoennLoennstrekkordningen",
            "verdi": "874869"
          },
          {
            "tekniskNavn": "personinntektLoenn",
            "verdi": "746315"
          },
          {
            "tekniskNavn": "svalbardPersoninntektNaering",
            "verdi": "696009"
          },
          {
            "tekniskNavn": "skatteoppgjoersdato",
            "verdi": "2017-08-09"
          }
        ]
        """;
    private String JSON_uten_skatteoppgjoersdato = """
        [
          {
            "tekniskNavn": "personinntektFiskeFangstFamiliebarnehage",
            "verdi": "814952"
          },
          {
            "tekniskNavn": "personinntektNaering",
            "verdi": "785896"
          },
          {
            "tekniskNavn": "personinntektBarePensjonsdel",
            "verdi": "844157"
          },
          {
            "tekniskNavn": "svalbardLoennLoennstrekkordningen",
            "verdi": "874869"
          },
          {
            "tekniskNavn": "personinntektLoenn",
            "verdi": "746315"
          },
          {
            "tekniskNavn": "svalbardPersoninntektNaering",
            "verdi": "696009"
          }
        ]
        """;

    @Test
    void skal_hente_og_mappe_om_data_fra_sigrun() {
        Year iFjor = Year.now().minusYears(1L);
        Year toÅrSiden = Year.now().minusYears(2L);
        Year treÅrSiden = Year.now().minusYears(3L);

        Mockito.when(client.hentBeregnetSkattForAktørOgÅr(AKTØR_ID, iFjor.toString()))
            .thenReturn(Arrays.asList(DefaultJsonMapper.fromJson(JSON, BeregnetSkatt[].class)));

        SigrunResponse beregnetskatt = consumer.beregnetskatt(AKTØR_ID, null);
        assertThat(beregnetskatt.beregnetSkatt()).hasSize(3);
        assertThat(beregnetskatt.beregnetSkatt().get(iFjor)).hasSize(7);
        assertThat(beregnetskatt.beregnetSkatt().get(toÅrSiden)).hasSize(0);
        assertThat(beregnetskatt.beregnetSkatt().get(treÅrSiden)).hasSize(0);
    }

    @Test
    void skal_hente_data_for_forifjor_når_skatteoppgjoersdato_mangler_for_ifjor() {
        Year iFjor = Year.now().minusYears(1L);
        Year toÅrSiden = Year.now().minusYears(2L);
        Year treÅrSiden = Year.now().minusYears(3L);
        Year fireÅrSiden = Year.now().minusYears(4L);

        Mockito.when(client.hentBeregnetSkattForAktørOgÅr(AKTØR_ID, iFjor.toString()))
            .thenReturn(Arrays.asList(DefaultJsonMapper.fromJson(JSON_uten_skatteoppgjoersdato, BeregnetSkatt[].class)));
        Mockito.when(client.hentBeregnetSkattForAktørOgÅr(AKTØR_ID, toÅrSiden.toString()))
            .thenReturn(Arrays.asList(DefaultJsonMapper.fromJson(JSON, BeregnetSkatt[].class)));

        SigrunResponse beregnetskatt = consumer.beregnetskatt(AKTØR_ID, null);
        assertThat(beregnetskatt.beregnetSkatt()).hasSize(3);
        assertThat(beregnetskatt.beregnetSkatt().get(iFjor)).isNull();
        assertThat(beregnetskatt.beregnetSkatt().get(toÅrSiden)).hasSize(7);
        assertThat(beregnetskatt.beregnetSkatt().get(treÅrSiden)).isEmpty();
        assertThat(beregnetskatt.beregnetSkatt().get(fireÅrSiden)).isEmpty();
    }

    @Test
    void skal_hente_summertskattegrunnlag() {
        Year iFjor = Year.now().minusYears(1L);
        var respons = DefaultJsonMapper.fromJson(JSON_summerskattegrunnlag, SSGResponse.class);

        Mockito.when(client.hentBeregnetSkattForAktørOgÅr(eq(AKTØR_ID), any()))
            .thenReturn(Arrays.asList(DefaultJsonMapper.fromJson(JSON, BeregnetSkatt[].class)));
        Mockito.when(client.hentSummertskattegrunnlag(AKTØR_ID, iFjor.toString())).thenReturn(Optional.of(respons));


        SigrunSummertSkattegrunnlagResponse response = consumer.summertSkattegrunnlag(AKTØR_ID, null);

        Map<Year, Optional<SSGResponse>> summertskattegrunnlagMap = response.summertskattegrunnlagMap();
        Optional<SSGResponse> sum = summertskattegrunnlagMap.get(iFjor);
        assertThat(sum).isPresent();
        SSGResponse ssgResponse = sum.get();
        assertThat(ssgResponse.grunnlag()).hasSize(4);
        assertThat(ssgResponse.svalbardGrunnlag()).hasSize(4);
        assertThat(ssgResponse.skatteoppgjoersdato()).isNotEmpty();
    }

    @Test
    void skal_utvide_beregningsperioden_når_fjoråret_ikke_er_ferdiglignet() {
        Year iFjor = Year.now().minusYears(1L);
        var respons = DefaultJsonMapper.fromJson(JSON_summerskattegrunnlag, SSGResponse.class);

        String jsonUtenFerdiglignetÅr = "[]";
        Mockito.when(client.hentBeregnetSkattForAktørOgÅr(eq(AKTØR_ID), any()))
            .thenReturn(Arrays.asList(DefaultJsonMapper.fromJson(jsonUtenFerdiglignetÅr, BeregnetSkatt[].class)));
        Mockito.when(client.hentSummertskattegrunnlag(AKTØR_ID, iFjor.toString())).thenReturn(Optional.of(respons));


        SigrunConsumerImpl consumer = new SigrunConsumerImpl(client);
        List<Year> årDetHentesFor = consumer.hentÅrsListeForSummertskattegrunnlag(AKTØR_ID,
            IntervallEntitet.fraOgMedTilOgMed(iFjor.atDay(1), iFjor.atMonthDay(MonthDay.of(12, 31))));

        assertThat(årDetHentesFor).containsOnly(iFjor, iFjor.minusYears(1));

    }

    @Test
    void skal_ikke_utvide_beregningsperioden_når_fjoråret_er_ferdiglignet() {
        Year iFjor = Year.now().minusYears(1L);
        var respons = DefaultJsonMapper.fromJson(JSON_summerskattegrunnlag, SSGResponse.class);

        String jsonMedFerdiglignetÅr = JSON;
        Mockito.when(client.hentBeregnetSkattForAktørOgÅr(eq(AKTØR_ID), any()))
            .thenReturn(Arrays.asList(DefaultJsonMapper.fromJson(jsonMedFerdiglignetÅr, BeregnetSkatt[].class)));
        Mockito.when(client.hentSummertskattegrunnlag(AKTØR_ID, iFjor.toString())).thenReturn(Optional.of(respons));


        SigrunConsumerImpl consumer = new SigrunConsumerImpl(client);
        List<Year> årDetHentesFor = consumer.hentÅrsListeForSummertskattegrunnlag(AKTØR_ID,
            IntervallEntitet.fraOgMedTilOgMed(iFjor.atDay(1), iFjor.atMonthDay(MonthDay.of(12, 31))));

        assertThat(årDetHentesFor).containsOnly(iFjor);

    }

}
