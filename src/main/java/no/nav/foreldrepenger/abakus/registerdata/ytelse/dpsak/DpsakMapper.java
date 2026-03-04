package no.nav.foreldrepenger.abakus.registerdata.ytelse.dpsak;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

public class DpsakMapper {

    private DpsakMapper() {
    }

    static List<DpsakVedtak> fullMapping(List<DagpengerRettighetsperioderDto.Rettighetsperiode> perioder,
                                         List<DagpengerUtbetalingDto> utbetalinger) {
        var rettighetsperioder = Optional.ofNullable(perioder).orElseGet(List::of);
        var safeUtbetalinger = Optional.ofNullable(utbetalinger).orElseGet(List::of);
        // Deler opp rettighetsperioder etter dagsats og utbetalingsdatoer med dagsats. Lager vedtak fra disse
        var vedtakene = mapDatadelingVedtak(rettighetsperioder, safeUtbetalinger).stream()
            .map(v -> new DpsakVedtak(v.getLocalDateInterval(), v.getValue(), new LinkedHashSet<>()))
            .toList();
        // Henter utbetalingstidslinje med summert ubetalt og helge-extender disse
        mapDatadelingDpsakUtbetaling(safeUtbetalinger).stream().forEach((u -> {
            var vedtak = vedtakene.stream().filter(v -> Objects.equals(v.dagsats(), u.getValue().sats()) && v.periode().overlaps(u.getLocalDateInterval())).findFirst().orElseThrow();
            var du = new DpsakVedtak.DpsakUtbetaling(u.getLocalDateInterval().extendThroughWeekend(),
                u.getValue().sats(), u.getValue().utbetaltBeløp(), u.getValue().sumUtbetalt());
            vedtak.utbetalinger().add(du);
        }));
        return vedtakene;
    }

    private static LocalDateTimeline<Integer> mapDatadelingVedtak(List<DagpengerRettighetsperioderDto.Rettighetsperiode> rettighetsperioder,
                                                                  List<DagpengerUtbetalingDto> utbetalinger) {
        // Tidslinje for rettighetsperioder
        var periodertidslinje = rettighetsperioder.stream()
            .map(p -> new LocalDateSegment<>(p.fraOgMedDato(), p.tilOgMedDato(), 0))
            .collect(Collectors.collectingAndThen(Collectors.toList(),
                l -> new LocalDateTimeline<>(l, StandardCombinators::max).compress()));
        // Tidslinje for 0-padding
        var dagsats0tidslinje = new LocalDateTimeline<>(new LocalDateInterval(null, null), 0);
        if (utbetalinger.isEmpty()) {
            return dagsats0tidslinje.intersection(periodertidslinje).compress();
        } else {
            // Extender tidligste og seneste utbetalingsdato
            var minUtbetalt = utbetalinger.stream().map(DagpengerUtbetalingDto::fraOgMed).min(Comparator.naturalOrder()).orElseThrow();
            var maxUtbetalt = utbetalinger.stream().map(DagpengerUtbetalingDto::tilOgMed).max(Comparator.naturalOrder()).orElseThrow();
            var segmenter = utbetalinger.stream()
                .map(utbetaling -> new LocalDateSegment<>(fomEllerNull(minUtbetalt, utbetaling),
                    tomEllerNull(maxUtbetalt, utbetaling), utbetaling.sats()))
                .toList();
            var dagsatstidslinje = new LocalDateTimeline<>(segmenter, StandardCombinators::max).compress();
            // 0-padding av manglende utbetalinger - dagsats 0
            var samletDagsatsTidslinje = dagsatstidslinje.union(dagsats0tidslinje, StandardCombinators::max).compress();
            return samletDagsatsTidslinje.intersection(periodertidslinje).compress();
        }
    }

    private static LocalDate fomEllerNull(LocalDate min, DagpengerUtbetalingDto utbetaling) {
        return min.equals(utbetaling.fraOgMed()) ? null : utbetaling.fraOgMed();
    }

    private static LocalDate tomEllerNull(LocalDate max, DagpengerUtbetalingDto utbetaling) {
        return max.equals(utbetaling.tilOgMed()) ? null : utbetaling.tilOgMed();
    }

    private record MappedUtbetaling(Integer sats, Integer utbetaltBeløp, Integer sumUtbetalt) implements Comparable<MappedUtbetaling> {

        MappedUtbetaling(DagpengerUtbetalingDto utbetaling) {
            this(utbetaling.sats(), utbetaling.utbetaltBeløp(), utbetaling.utbetaltBeløp());
        }

        @Override
        public int compareTo(MappedUtbetaling o) {
            var utbetaltDiff = Integer.compare(this.utbetaltBeløp, o.utbetaltBeløp);
            return utbetaltDiff == 0 ? Integer.compare(this.sats, o.sats) : utbetaltDiff;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            return o instanceof MappedUtbetaling that && sats.equals(that.sats) && utbetaltBeløp.equals(that.utbetaltBeløp);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sats, utbetaltBeløp);
        }
    }

    private static LocalDateTimeline<MappedUtbetaling> mapDatadelingDpsakUtbetaling(List<DagpengerUtbetalingDto> utbetalinger) {
        var mapped = utbetalinger.stream()
            .filter(u -> u.utbetaltBeløp() > 0 || u.fraOgMed().getDayOfWeek().compareTo(DayOfWeek.SATURDAY) < 0
                || u.tilOgMed().getDayOfWeek().compareTo(DayOfWeek.SATURDAY) < 0)
            .map(u -> new LocalDateSegment<>(new LocalDateInterval(u.fraOgMed(), u.tilOgMed()), new MappedUtbetaling(u)))
            .collect(Collectors.collectingAndThen(Collectors.toList(), l -> new LocalDateTimeline<>(l, StandardCombinators::max)));
        // Slå sammen dager med lik dagsats og utbetaling. Utvide fredager til søndag pga filter over. Summer sumUtbetalt ved sammenslåing
        return mapped.compress(LocalDateInterval::abutsWorkdays, MappedUtbetaling::equals, DpsakMapper::slåSammen);
    }

    private static LocalDateSegment<MappedUtbetaling> slåSammen(LocalDateInterval i,
                                                                LocalDateSegment<MappedUtbetaling> lhs,
                                                                LocalDateSegment<MappedUtbetaling> rhs) {
        return new LocalDateSegment<>(i, new MappedUtbetaling(lhs.getValue().sats(), lhs.getValue().utbetaltBeløp(),
            lhs.getValue().sumUtbetalt() + rhs.getValue().utbetaltBeløp()));
    }

}
