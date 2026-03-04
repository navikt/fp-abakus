package no.nav.foreldrepenger.abakus.registerdata.ytelse.dpsak;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class DpsakMapperTest {

    @Test
    void ingen_utbetalinger() {
        var startdato = LocalDate.of(2025, Month.SEPTEMBER,1);
        var periode = new DagpengerRettighetsperioderDto.Rettighetsperiode(startdato, null,
            DagpengerRettighetsperioderDto.DagpengerKilde.DP_SAK);
        var vedtak = DpsakMapper.fullMapping(List.of(periode), List.of());
        assertThat(vedtak).hasSize(1);
        var v1 = vedtak.getFirst();
        assertThat(v1.periode().getFomDato()).isEqualTo(startdato);
        assertThat(v1.dagsats()).isZero();
        assertThat(v1.utbetalinger()).isEmpty();
    }


    @Test
    void testcase1_mapping() throws Exception {
        var startdato = LocalDate.of(2025, Month.SEPTEMBER,1);
        var nyttår = LocalDate.of(2026, Month.JANUARY,1);
        var periode = new DagpengerRettighetsperioderDto.Rettighetsperiode(startdato, null,
            DagpengerRettighetsperioderDto.DagpengerKilde.DP_SAK);
        try (var resource = DpsakMapper.class.getResourceAsStream("/dpsak/testcase1.json")) {
            assertThat(resource).isNotNull();
            var utbetalinger = DefaultJsonMapper.listFromJson(new String(resource.readAllBytes()), DagpengerUtbetalingDto.class);
            var vedtak = DpsakMapper.fullMapping(List.of(periode), utbetalinger);
            assertThat(vedtak).hasSize(2);
            var v1 = vedtak.getFirst();
            var v2 = vedtak.getLast();
            assertThat(v1.periode().getFomDato()).isEqualTo(startdato);
            assertThat(v1.periode().getTomDato()).isEqualTo(nyttår.minusDays(1));
            assertThat(v1.dagsats()).isEqualTo(1423);
            assertThat(v1.utbetalinger()).hasSize(9);
            assertThat(v2.periode().getFomDato()).isEqualTo(nyttår);
            assertThat(v2.periode().getTomDato()).isEqualTo(LocalDateInterval.TIDENES_ENDE);
            assertThat(v2.dagsats()).isEqualTo(1424);
            assertThat(v2.utbetalinger()).hasSize(3);
        }
    }

}
