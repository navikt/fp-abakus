package no.nav.foreldrepenger.abakus.registerdata.ytelse.kelvin;

import static java.time.temporal.TemporalAdjusters.next;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class KelvinMapperTest {

    @Test
    void ingen_utbetalinger() {
        var startdato = LocalDate.now().plusWeeks(1);
        var periode = new ArbeidsavklaringspengerResponse.AAPVedtak(0, 0, 390000,
            1000, 1000, ArbeidsavklaringspengerResponse.Kildesystem.KELVIN,
            new ArbeidsavklaringspengerResponse.AAPPeriode(startdato, startdato.plusYears(1)),
            "ABCDE", "LØPENDE", "vedtakid", startdato, List.of());
        var vedtak = KelvinMapper.mapTilMeldekortAclKelvin(List.of(periode), new Saksnummer("VAARSAK"));
        assertThat(vedtak).hasSize(1);
        var v1 = vedtak.getFirst();
        assertThat(v1.getVedtaksPeriodeFom()).isEqualTo(startdato);
        assertThat(v1.getVedtaksDagsats().getVerdi().intValue()).isEqualTo(1000);
        assertThat(v1.getMeldekortene()).isEmpty();
    }


    @Test
    void barnetillegg_forsvinner_delvis_og_full_mapping() throws Exception {
        var startdato1 = LocalDate.of(2025, Month.FEBRUARY,24);
        var startdato2 = LocalDate.of(2025, Month.MARCH,7);
        var sluttdato = LocalDate.of(2026, Month.APRIL,3);
        try (var resource = KelvinMapper.class.getResourceAsStream("/kelvin/testcase1.json")) {
            assertThat(resource).isNotNull();
            var aaprespons = DefaultJsonMapper.fromJson(new String(resource.readAllBytes()), ArbeidsavklaringspengerResponse.class);
            var vedtak = KelvinMapper.mapTilMeldekortAclKelvin(aaprespons.vedtak(), new Saksnummer("VAARSAK"));
            assertThat(vedtak).hasSize(2);
            // Første del med barnetillegg til barn blir 18
            var v1 = vedtak.getFirst();
            // Andre del uten barnetillegg
            var v2 = vedtak.getLast();
            assertThat(v1.getVedtaksPeriodeFom()).isEqualTo(startdato1);
            assertThat(v1.getVedtaksPeriodeTom()).isEqualTo(startdato2.minusDays(1));
            assertThat(v1.getVedtaksDagsats().getVerdi().intValue()).isEqualTo(1435);
            assertThat(v1.getMeldekortene()).hasSize(1);
            var v1mk1 = v1.getMeldekortene().getFirst();
            assertThat(v1mk1.getMeldekortFom()).isEqualTo(startdato1);
            assertThat(v1mk1.getBeløp().intValue()).isEqualTo(9549);
            assertThat(v1mk1.getDagsats().intValue()).isEqualTo(1435);
            assertThat(v1mk1.getUtbetalingsgrad().intValue()).isEqualTo(74);
            assertThat(v2.getVedtaksPeriodeFom()).isEqualTo(startdato2);
            assertThat(v2.getVedtaksPeriodeTom()).isEqualTo(sluttdato);
            assertThat(v2.getVedtaksDagsats().getVerdi().intValue()).isEqualTo(1398);
            assertThat(v2.getMeldekortene()).hasSize(3);
            var v2mk1 = v2.getMeldekortene().getFirst();
            assertThat(v2mk1.getMeldekortFom()).isEqualTo(startdato2);
            assertThat(v2mk1.getBeløp().intValue()).isEqualTo(1034);
            assertThat(v2mk1.getDagsats().intValue()).isEqualTo(1398);
            assertThat(v2mk1.getUtbetalingsgrad().intValue()).isEqualTo(74);
            var v2mk2 = v2.getMeldekortene().get(1);
            assertThat(v2mk2.getMeldekortFom()).isEqualTo(startdato2.with(next(DayOfWeek.MONDAY)));
            assertThat(v2mk2.getBeløp().intValue()).isEqualTo(10620);
            assertThat(v2mk2.getDagsats().intValue()).isEqualTo(1398);
            assertThat(v2mk2.getUtbetalingsgrad().intValue()).isEqualTo(76);
            // Periode på bortimot ett år - men ok siden samme sats og utbetalingsprosent.
            var v2mk3 = v2.getMeldekortene().getLast();
            assertThat(v2mk3.getMeldekortFom()).isEqualTo(startdato1.plusMonths(1));
            assertThat(v2mk3.getBeløp().intValue()).isEqualTo(363480);
            assertThat(v2mk3.getDagsats().intValue()).isEqualTo(1398);
            assertThat(v2mk3.getUtbetalingsgrad().intValue()).isEqualTo(100);
        }
    }

    @Test
    void samordning_uføre() throws Exception {
        var startdato = LocalDate.of(2025, Month.SEPTEMBER,8);
        var sluttdato = LocalDate.of(2026, Month.SEPTEMBER,7);
        try (var resource = KelvinMapper.class.getResourceAsStream("/kelvin/testcase2.json")) {
            assertThat(resource).isNotNull();
            var aaprespons = DefaultJsonMapper.fromJson(new String(resource.readAllBytes()), ArbeidsavklaringspengerResponse.class);
            var vedtak = KelvinMapper.mapTilMeldekortAclKelvin(aaprespons.vedtak(), new Saksnummer("VAARSAK"));
            assertThat(vedtak).hasSize(1);
            // Skal bruke dagsatsen som gjelder AAP etter samordning med uføre og utbetaling relativt til den. 1031 = 60% av 1719
            var v1 = vedtak.getFirst();
            assertThat(v1.getVedtaksPeriodeFom()).isEqualTo(startdato);
            assertThat(v1.getVedtaksPeriodeTom()).isEqualTo(sluttdato);
            assertThat(v1.getVedtaksDagsats().getVerdi().intValue()).isEqualTo(1031);
            assertThat(v1.getMeldekortene()).hasSize(1);
            var v1mk1 = v1.getMeldekortene().getFirst();
            assertThat(v1mk1.getMeldekortFom()).isEqualTo(startdato);
            assertThat(v1mk1.getBeløp().intValue()).isEqualTo(41240);
            assertThat(v1mk1.getDagsats().intValue()).isEqualTo(1031); // Etter Uføre
            assertThat(v1mk1.getUtbetalingsgrad().intValue()).isEqualTo(100);

        }
    }


}
