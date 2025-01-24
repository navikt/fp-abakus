package no.nav.foreldrepenger.abakus.registerdata.ytelse.arena;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.arena.respons.BeløpDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.arena.respons.FagsystemDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.arena.respons.MeldekortUtbetalingsgrunnlagMeldekortDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.arena.respons.MeldekortUtbetalingsgrunnlagSakDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.arena.respons.YtelseStatusDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.arena.respons.YtelseTypeDto;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import org.junit.jupiter.api.Test;

/** Konsistens test for å verifiser at seralisering og deseralisering av DTO ikke endrer seg og fungere som forventet */
class MeldekortUtbetalingsgrunnlagSakDtoSeraliseringOgDeseraliseringTest {

    private static String hardkodetSeralisertStreng() {
        return """
             {
                "kilde": "ARENA",
                "kravMottattDato": "2022-08-24",
                "meldekortene": [
                    {
                        "beløp": 10,
                        "dagsats": 1,
                        "meldekortFom": "2022-08-24",
                        "meldekortTom": "2022-08-29",
                        "utbetalingsgrad": 100
                    }
                ],
                "sakStatus": "AKTIV",
                "saksnummer": "1234567890",
                "tilstand": "LOP",
                "type": "DAG",
                "vedtakStatus": "IVERK",
                "vedtaksDagsats": 809.0,
                "vedtaksPeriodeFom": "2022-12-27",
                "vedtaksPeriodeTom": "2023-01-06",
                "vedtattDato": "2022-08-24"
            }
            """;
    }

    static MeldekortUtbetalingsgrunnlagSakDto getMeldekortUtbetalingsgrunnlagSakDto() {
        return new MeldekortUtbetalingsgrunnlagSakDto.Builder()
                .kilde(FagsystemDto.ARENA)
                .kravMottattDato(LocalDate.of(2022, 8, 24))
                .meldekortene(List.of(getMeldekortUtbetalingsgrunnlagMeldekortDto()))
                .sakStatus("AKTIV")
                .saksnummer("1234567890")
                .tilstand(YtelseStatusDto.LOP)
                .type(YtelseTypeDto.DAG)
                .vedtakStatus("IVERK")
                .vedtaksDagsats(new BeløpDto(BigDecimal.valueOf(809.0)))
                .vedtaksPeriodeFom(LocalDate.of(2022, 12, 27))
                .vedtaksPeriodeTom(LocalDate.of(2023, 1, 6))
                .vedtattDato(LocalDate.of(2022, 8, 24))
                .build();
    }

    private static MeldekortUtbetalingsgrunnlagMeldekortDto getMeldekortUtbetalingsgrunnlagMeldekortDto() {
        return new MeldekortUtbetalingsgrunnlagMeldekortDto.Builder()
                .beløp(BigDecimal.TEN)
                .dagsats(BigDecimal.ONE)
                .meldekortFom(LocalDate.of(2022, 8, 24))
                .meldekortTom(LocalDate.of(2022, 8, 29))
                .utbetalingsgrad(BigDecimal.valueOf(100))
                .build();
    }

    @Test
    void konsistenstestForÅSjekkeAtDeseraliseringFungereUavhengigAvSeralisering() {
        var seralisertStreng = hardkodetSeralisertStreng();
        var testA = DefaultJsonMapper.fromJson(seralisertStreng, MeldekortUtbetalingsgrunnlagSakDto.class);
        assertThat(testA).isEqualTo(getMeldekortUtbetalingsgrunnlagSakDto());
    }

    @Test
    void konsistenstestForÅSjekkeAtSeraliseringFungereUavhengigAvDeseralisering() {
        var meldekortUtbetalingsgrunnlagSakDto = getMeldekortUtbetalingsgrunnlagSakDto();
        var seralized = DefaultJsonMapper.toJson(meldekortUtbetalingsgrunnlagSakDto);
        assertThat(seralized)
                .isEqualToIgnoringWhitespace(hardkodetSeralisertStreng().replaceAll("[\n\r ]", ""));
    }
}
