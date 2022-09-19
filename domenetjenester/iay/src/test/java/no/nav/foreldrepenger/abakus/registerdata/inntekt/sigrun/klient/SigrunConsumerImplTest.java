package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Year;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SSGResponse;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SigrunSummertSkattegrunnlagResponse;

public class SigrunConsumerImplTest {

    private static final long AKTØR_ID = 123123L;
    private SigrunRestClient client = Mockito.mock(SigrunRestClient.class);

    private SigrunConsumer consumer = new SigrunConsumerImpl(client, null);

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

    @Test
    public void skal_hente_og_mappe_om_data_fra_sigrun() {
        Year iFjor = Year.now().minusYears(1L);
        Year toÅrSiden = Year.now().minusYears(2L);
        Year treÅrSiden = Year.now().minusYears(3L);

        Mockito.when(client.hentBeregnetSkattForAktørOgÅr(AKTØR_ID, iFjor.toString())).thenReturn(JSON);

        SigrunResponse beregnetskatt = consumer.beregnetskatt(AKTØR_ID);
        assertThat(beregnetskatt.beregnetSkatt()).hasSize(3);
        assertThat(beregnetskatt.beregnetSkatt().get(iFjor)).hasSize(7);
        assertThat(beregnetskatt.beregnetSkatt().get(toÅrSiden)).hasSize(0);
        assertThat(beregnetskatt.beregnetSkatt().get(treÅrSiden)).hasSize(0);
    }

    @Test
    public void skal_hente_data_for_forifjor_når_skatteoppgjoersdato_mangler_for_ifjor() {
        Year iFjor = Year.now().minusYears(1L);
        Year toÅrSiden = Year.now().minusYears(2L);
        Year treÅrSiden = Year.now().minusYears(3L);
        Year fireÅrSiden = Year.now().minusYears(4L);

        Mockito.when(client.hentBeregnetSkattForAktørOgÅr(AKTØR_ID, iFjor.toString())).thenReturn(JSON_uten_skatteoppgjoersdato);
        Mockito.when(client.hentBeregnetSkattForAktørOgÅr(AKTØR_ID, toÅrSiden.toString())).thenReturn(JSON);

        SigrunResponse beregnetskatt = consumer.beregnetskatt(AKTØR_ID);
        assertThat(beregnetskatt.beregnetSkatt()).hasSize(3);
        assertThat(beregnetskatt.beregnetSkatt().get(iFjor)).isNull();
        assertThat(beregnetskatt.beregnetSkatt().get(toÅrSiden)).hasSize(7);
        assertThat(beregnetskatt.beregnetSkatt().get(treÅrSiden)).hasSize(0);
        assertThat(beregnetskatt.beregnetSkatt().get(fireÅrSiden)).hasSize(0);
    }

    @Test
    public void skal_hente_summertskattegrunnlag() {
        Year iFjor = Year.now().minusYears(1L);

        Mockito.when(client.hentSummertskattegrunnlag(AKTØR_ID, iFjor.toString())).thenReturn(JSON_summerskattegrunnlag);

        SigrunSummertSkattegrunnlagResponse response = consumer.summertSkattegrunnlag(AKTØR_ID);

        Map<Year, Optional<SSGResponse>> summertskattegrunnlagMap = response.summertskattegrunnlagMap();
        Optional<SSGResponse> sum = summertskattegrunnlagMap.get(iFjor);
        assertThat(sum).isPresent();
        SSGResponse ssgResponse = sum.get();
        assertThat(ssgResponse.grunnlag()).hasSize(4);
        assertThat(ssgResponse.svalbardGrunnlag()).hasSize(4);
        assertThat(ssgResponse.skatteoppgjoersdato()).isNotEmpty();
    }

}
