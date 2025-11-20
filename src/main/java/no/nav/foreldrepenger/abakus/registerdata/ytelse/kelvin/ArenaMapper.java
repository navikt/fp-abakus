package no.nav.foreldrepenger.abakus.registerdata.ytelse.kelvin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagMeldekort;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.konfig.Tid;

public class ArenaMapper {
    private static final Logger LOG = LoggerFactory.getLogger(ArenaMapper.class);

    private ArenaMapper() {
    }

    static List<MeldekortUtbetalingsgrunnlagSak> mapTilMeldekortAclArena(List<ArbeidsavklaringspengerResponse.AAPVedtak> vedtak,
                                                                                 LocalDate opplysningFom) {
        return vedtak.stream()
            .map(v -> mapTilMeldekortSakAclArena(v, opplysningFom))
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(MeldekortUtbetalingsgrunnlagSak::getVedtaksPeriodeFom))
            .toList();
    }

    private static MeldekortUtbetalingsgrunnlagSak mapTilMeldekortSakAclArena(ArbeidsavklaringspengerResponse.AAPVedtak vedtak,
                                                                              LocalDate opplysningFom) {
        var mk = vedtak.utbetaling().stream()
            .map(ArenaMapper::mapTilMeldekortMKAclArena)
            .sorted(Comparator.comparing(MeldekortUtbetalingsgrunnlagMeldekort::getMeldekortFom))
            .toList();
        if (mk.isEmpty() && vedtak.vedtaksdato().isBefore(opplysningFom) && // Kan hende det holder med sjekk på periode/tilOgMedDato / null
            (vedtak.periode() == null || vedtak.periode().tilOgMedDato() == null || vedtak.periode().fraOgMedDato() == null)) {
            return null;
        }
        var vedtaksdagsats = Optional.ofNullable(vedtak.dagsatsEtterUføreReduksjon()).or(() -> Optional.ofNullable(vedtak.dagsats())).orElse(0);
        var vedtaksdagsatsMedBarnetillegg = vedtaksdagsats + Optional.ofNullable(vedtak.barnetillegg()).orElse(0);
        return MeldekortUtbetalingsgrunnlagSak.MeldekortSakBuilder.ny()
            .leggTilMeldekort(mk)
            .medType(YtelseType.ARBEIDSAVKLARINGSPENGER)
            .medTilstand(KelvinKlient.tilTilstand(vedtak.status()))
            .medKilde(Fagsystem.ARENA)
            .medSaksnummer(Optional.ofNullable(vedtak.saksnummer()).map(Saksnummer::new).orElse(null))
            .medKravMottattDato(vedtak.vedtaksdato())
            .medVedtattDato(vedtak.vedtaksdato())
            .medVedtaksPeriodeFom(Tid.fomEllerMin(vedtak.periode().fraOgMedDato()))
            .medVedtaksPeriodeTom(Tid.tomEllerMax(vedtak.periode().tilOgMedDato()))
            .medVedtaksDagsats(BigDecimal.valueOf(vedtaksdagsatsMedBarnetillegg))
            .build();
    }

    private static MeldekortUtbetalingsgrunnlagMeldekort mapTilMeldekortMKAclArena(ArbeidsavklaringspengerResponse.AAPUtbetaling utbetaling) {
        var beregnetUtbetalingsgrad = regnUtArenaUtbetalingsgrad(utbetaling);
        var utbetalingsgradFraUtbetaling = Optional.ofNullable(utbetaling.utbetalingsgrad()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO);
        if (beregnetUtbetalingsgrad.compareTo(utbetalingsgradFraUtbetaling) != 0) {
            LOG.info("Kelvin-saker arena avvik utbetalingsgrad utbetaling {}: beregnet {}, oppgitt {}",
                utbetaling, beregnetUtbetalingsgrad, utbetaling.utbetalingsgrad());
        }
        return MeldekortUtbetalingsgrunnlagMeldekort.MeldekortMeldekortBuilder.ny()
            .medMeldekortFom(Tid.fomEllerMin(utbetaling.periode().fraOgMedDato()))
            .medMeldekortTom(Tid.tomEllerMax(utbetaling.periode().tilOgMedDato()))
            .medBeløp(Optional.ofNullable(utbetaling.belop()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO))
            .medDagsats(Optional.ofNullable(utbetaling.dagsats()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO))
            .medUtbetalingsgrad(beregnetUtbetalingsgrad)
            .build();
    }

    private static BigDecimal regnUtArenaUtbetalingsgrad(ArbeidsavklaringspengerResponse.AAPUtbetaling utbetaling) {
        var beløp = Optional.ofNullable(utbetaling.belop()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO);
        var dagsats = Optional.ofNullable(utbetaling.dagsats()).map(BigDecimal::valueOf).orElse(BigDecimal.ONE);
        var virkedager = beregnVirkedager(utbetaling.periode().fraOgMedDato(), utbetaling.periode().tilOgMedDato());
        return beløp.multiply(BigDecimal.valueOf(200)).divide(dagsats.multiply(BigDecimal.valueOf(virkedager)), 1, RoundingMode.HALF_UP);
    }

    private static int beregnVirkedager(LocalDate fom, LocalDate tom) {
        try {
            var padBefore = fom.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
            var padAfter = DayOfWeek.SUNDAY.getValue() - tom.getDayOfWeek().getValue();
            var virkedagerPadded = Math.toIntExact(
                ChronoUnit.WEEKS.between(fom.minusDays(padBefore), tom.plusDays(padAfter).plusDays(1L)) * 5L);
            var virkedagerPadding = Math.min(padBefore, 5) + Math.max(padAfter - 2, 0);
            return virkedagerPadded - virkedagerPadding;
        } catch (ArithmeticException var6) {
            throw new UnsupportedOperationException("Perioden er for lang til å beregne virkedager.", var6);
        }
    }
}
