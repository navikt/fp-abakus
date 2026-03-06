package no.nav.foreldrepenger.abakus.registerdata.ytelse.dpsak;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.fpsak.tidsserie.StandardCombinators;

public class DpsakMapper {

    private DpsakMapper() {
    }

    static List<DpsakVedtak> fullMapping(List<DagpengerRettighetsperioderDto.Rettighetsperiode> rettighetsperioder,
                                         List<DagpengerUtbetalingDto> utbetalinger) {
        // Deler opp rettighetsperioder etter dagsats og utbetalingsdatoer med dagsats. Lager vedtak fra disse
        var vedtakene = mapDatadelingVedtak(rettighetsperioder, utbetalinger).stream()
            .map(v -> new DpsakVedtak(v.getLocalDateInterval(), v.getValue(), new ArrayList<>()))
            .toList();
        // Henter utbetalingstidslinje med summert ubetalt og helge-extender disse
        mapDatadelingDpsakUtbetaling(utbetalinger).forEach(u -> {
            var vedtak = vedtakene.stream()
                .filter(v -> Objects.equals(v.dagsats(), u.getValue().sats()) && v.periode().overlaps(u.getLocalDateInterval()))
                .findFirst().orElseThrow(() -> new IllegalStateException("Fant ikke utbetaling " + u + " i noen vedtak " + vedtakene));
            var du = new DpsakVedtak.DpsakUtbetaling(u.getLocalDateInterval().extendThroughWeekend(),
                u.getValue().sats(), u.getValue().utbetaltBeløp(), u.getValue().sumUtbetalt());
            vedtak.utbetalinger().add(du);
        });
        return vedtakene;
    }

    private static LocalDateTimeline<Integer> mapDatadelingVedtak(List<DagpengerRettighetsperioderDto.Rettighetsperiode> rettighetsperioder,
                                                                  List<DagpengerUtbetalingDto> utbetalinger) {
        // Tidslinje for rettighetsperioder
        var rettighetsperioderTidslinje = rettighetsperioder.stream()
            .map(p -> new LocalDateSegment<>(p.fraOgMedDato(), p.tilOgMedDato(), 0))
            .collect(Collectors.collectingAndThen(Collectors.toList(),
                l -> new LocalDateTimeline<>(l, StandardCombinators::max)))
            .compress();
        if (utbetalinger.isEmpty()) {
            return rettighetsperioderTidslinje;
        } else {
            // Extender seneste utbetalingsdato og antar at siste dagsats gjelder framover.
            var maxUtbetalt = utbetalinger.stream().map(DagpengerUtbetalingDto::tilOgMed).max(Comparator.naturalOrder()).orElseThrow();
            var utbetalingdagsatsTidslinje = utbetalinger.stream()
                .map(utbetaling -> new LocalDateSegment<>(utbetaling.fraOgMed(),
                    tomEllerNull(maxUtbetalt, utbetaling), utbetaling.sats()))
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                    l -> new LocalDateTimeline<>(l, StandardCombinators::max)))
                .compress();
            var rettighetsperioderUtenDagsats = rettighetsperioderTidslinje.disjoint(utbetalingdagsatsTidslinje);
            if (rettighetsperioderUtenDagsats.isEmpty()) {
                return rettighetsperioderTidslinje.intersection(utbetalingdagsatsTidslinje, StandardCombinators::max);
            } else {
                throw new IllegalStateException("Mangler utbetalinger for rettighetsperioder: " + rettighetsperioderUtenDagsats);
            }
        }
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
            .filter(DpsakMapper::virkedagEllerUtbetaling)
            .map(u -> new LocalDateSegment<>(new LocalDateInterval(u.fraOgMed(), u.tilOgMed()), new MappedUtbetaling(u)))
            .collect(Collectors.collectingAndThen(Collectors.toList(), l -> new LocalDateTimeline<>(l, StandardCombinators::max)));
        // Slå sammen dager med lik dagsats og utbetaling. Utvide fredager til søndag pga filter over. Summer sumUtbetalt ved sammenslåing
        return mapped.compress(LocalDateInterval::abutsWorkdays, MappedUtbetaling::equals, DpsakMapper::slåSammen);
    }

    private static boolean virkedagEllerUtbetaling(DagpengerUtbetalingDto utbetaling) {
        return utbetaling.utbetaltBeløp() > 0 || utbetaling.fraOgMed().getDayOfWeek().compareTo(DayOfWeek.SATURDAY) < 0
            || utbetaling.tilOgMed().getDayOfWeek().compareTo(DayOfWeek.SATURDAY) < 0;
    }

    private static LocalDateSegment<MappedUtbetaling> slåSammen(LocalDateInterval i,
                                                                LocalDateSegment<MappedUtbetaling> lhs,
                                                                LocalDateSegment<MappedUtbetaling> rhs) {
        return new LocalDateSegment<>(i, new MappedUtbetaling(lhs.getValue().sats(), lhs.getValue().utbetaltBeløp(),
            lhs.getValue().sumUtbetalt() + rhs.getValue().utbetaltBeløp()));
    }

}
