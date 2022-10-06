package no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.rs;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

/**
 * Konsistens test for å verifiser at seralisering og deseralisering av DTO ikke endrer seg og fungere som forventet
 */
class MeldekortUtbetalingsgrunnlagSakDtoSeraliseringOgDeseraliseringTest {

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
        assertThat(seralized).isEqualToIgnoringWhitespace(hardkodetSeralisertStreng().replaceAll("[\n\r ]", ""));
    }

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
                        "utbetalingsgrad": 10
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
        return new MeldekortUtbetalingsgrunnlagSakDto(
            Fagsystem.ARENA,
            LocalDate.of(2022, 8, 24),
            List.of(getMeldekortUtbetalingsgrunnlagMeldekortDto()),
            "AKTIV",
            "1234567890",
            YtelseStatus.LØPENDE,
            YtelseType.DAGPENGER,
            "IVERK",
            new BeløpDto(BigDecimal.valueOf(809.0)),
            LocalDate.of(2022, 12, 27),
            LocalDate.of(2023, 01, 6),
            LocalDate.of(2022, 8, 24));
    }

    private static MeldekortUtbetalingsgrunnlagMeldekortDto getMeldekortUtbetalingsgrunnlagMeldekortDto() {
        return new MeldekortUtbetalingsgrunnlagMeldekortDto(
            BigDecimal.TEN, BigDecimal.ONE,
            LocalDate.of(2022, 8, 24),
            LocalDate.of(2022, 8, 29),
            BigDecimal.TEN);
    }
}
