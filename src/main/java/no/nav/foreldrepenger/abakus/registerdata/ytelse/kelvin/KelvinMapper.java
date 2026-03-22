package no.nav.foreldrepenger.abakus.registerdata.ytelse.kelvin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagMeldekort;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.vedtak.konfig.Tid;

public class KelvinMapper {
    private static final Logger LOG = LoggerFactory.getLogger(KelvinMapper.class);
    private static final Environment ENV = Environment.current();

    private KelvinMapper() {
    }

    static List<MeldekortUtbetalingsgrunnlagSak> mapTilMeldekortAclKelvin(List<ArbeidsavklaringspengerResponse.AAPVedtak> vedtak, Saksnummer saksnummer) {
        var mapped = vedtak.stream()
            .map(KelvinMapper::mapTilMeldekortSakAclKelvin)
            .sorted(Comparator.comparing(MeldekortUtbetalingsgrunnlagSak::getVedtaksPeriodeFom))
            .toList();
        if (!mapped.isEmpty()) {
            if (vedtak.stream().anyMatch(v -> !Objects.equals(v.dagsats(), v.dagsatsEtterUføreReduksjon()))) {
                // TODO: Vurder exception for å studere disse nærmere.
                LOG.info("Kelvin-saker Kelvin UFO sak {} kilde {} mapped {}.", saksnummer.getVerdi(), vedtak, mapped);
            } else {
                LOG.info("Kelvin-saker Kelvin for sak {} kilde {} mapped {}.", saksnummer.getVerdi(), vedtak, mapped);
            }
        }
        return mapped;
    }

    private static MeldekortUtbetalingsgrunnlagSak mapTilMeldekortSakAclKelvin(ArbeidsavklaringspengerResponse.AAPVedtak vedtak) {
        var aktuellDagsats = Optional.ofNullable(vedtak.dagsatsEtterUføreReduksjon()).orElseGet(vedtak::dagsats);
        var aktuellDagsatsMedBarnetillegg = aktuellDagsats + Optional.ofNullable(vedtak.barnetillegg()).orElse(0);
        var mappedUtbetaling = mapDatadelingDpsakUtbetaling(vedtak.utbetaling());
        var mk = mappedUtbetaling.stream()
            .map(u -> KelvinMapper.mapTilMeldekortMKAclKelvin(u, aktuellDagsats, vedtak.dagsats(), aktuellDagsatsMedBarnetillegg))
            .sorted(Comparator.comparing(MeldekortUtbetalingsgrunnlagMeldekort::getMeldekortFom))
            .toList();
        return MeldekortUtbetalingsgrunnlagSak.MeldekortSakBuilder.ny()
            .leggTilMeldekort(mk)
            .medType(YtelseType.ARBEIDSAVKLARINGSPENGER)
            .medTilstand(KelvinKlient.tilTilstand(vedtak.status()))
            .medKilde(Fagsystem.KELVIN)
            .medSaksnummer(Optional.ofNullable(vedtak.saksnummer()).map(Saksnummer::new).orElse(null))
            .medKravMottattDato(vedtak.vedtaksdato())
            .medVedtattDato(vedtak.vedtaksdato())
            .medVedtaksPeriodeFom(Tid.fomEllerMin(vedtak.periode().fraOgMedDato()))
            .medVedtaksPeriodeTom(Tid.tomEllerMax(vedtak.periode().tilOgMedDato()))
            .medVedtaksDagsats(BigDecimal.valueOf(aktuellDagsatsMedBarnetillegg))
            .build();
    }

    private static MeldekortUtbetalingsgrunnlagMeldekort mapTilMeldekortMKAclKelvin(LocalDateSegment<MappedUtbetaling> utbetaling,
                                                                                    Integer aktuellDagsats, Integer vedtakDagsats,
                                                                                    Integer nettoDagsatsMedTillegg) {
        var utbetalingsgradFraUtbetaling = Optional.ofNullable(utbetaling.getValue().utbetalingProsent()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO);
        var brukUtbetalingsgrad = justertUtbetalingsgrad(utbetalingsgradFraUtbetaling, aktuellDagsats, vedtakDagsats);
        // Bruker denne fordi utbetaling.dagsats (og barnetillegg) er ferdig redusert med utbetalingsgrad fra kilden
        var dagsats = Optional.ofNullable(nettoDagsatsMedTillegg).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO);
        return MeldekortUtbetalingsgrunnlagMeldekort.MeldekortMeldekortBuilder.ny()
            .medMeldekortFom(Tid.fomEllerMin(utbetaling.getLocalDateInterval().getFomDato()))
            .medMeldekortTom(Tid.tomEllerMax(utbetaling.getLocalDateInterval().getTomDato()))
            .medBeløp(Optional.ofNullable(utbetaling.getValue().sumUtbetalt()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO))
            .medDagsats(dagsats)
            .medUtbetalingsgrad(brukUtbetalingsgrad)
            .build();
    }

    private static BigDecimal justertUtbetalingsgrad(BigDecimal utbetalingsgrad, Integer aktuellDagsats, Integer vedtakDagsats) {
        if (Objects.equals(aktuellDagsats, vedtakDagsats)) {
            return utbetalingsgrad;
        } else {
            if (!ENV.isLocal())
                LOG.warn("Merk Dem! Kelvin-tilfelle med uføresamordning. Si fra i daglig overvåkning for nærmere analyse");
            // Her vil tilfelle med uførereduksjon ha en ubetalingsgrad mellom 0 og 100 gitt av uførereduksjonen + aktivitet i perioden
            // Gjør derfor en normalisering slik at bruker med 60% AAP får utbetalingsgrad 100% ved full AAP-utbetaling uten aktivitet
            var beregnetUtbetalingsgrad = utbetalingsgrad
                .multiply(BigDecimal.valueOf(vedtakDagsats)).divide(BigDecimal.valueOf(aktuellDagsats), 0, RoundingMode.HALF_EVEN);
            LOG.info("Kelvin-saker Kelvin UFO beregnet utbetgrad: Utbetalingsgrad {} beregnet {} redusertvedtaksats {} vedtaksats {}",
                utbetalingsgrad, beregnetUtbetalingsgrad, aktuellDagsats, vedtakDagsats);
            return beregnetUtbetalingsgrad;
        }
    }

    private record MappedUtbetaling(Integer utbetalingProsent, Integer utbetaltForDag, Integer sumUtbetalt) implements Comparable<MappedUtbetaling> {

        MappedUtbetaling(ArbeidsavklaringspengerResponse.AAPUtbetaling utbetaling) {
            this(utbetaling.utbetalingsgrad(), utbetaling.dagsats() + utbetaling.barnetillegg(), utbetaling.belop());
        }

        @Override
        public int compareTo(MappedUtbetaling o) {
            var utbetaltDiff = Integer.compare(this.utbetaltForDag, o.utbetaltForDag);
            return utbetaltDiff == 0 ? Integer.compare(this.utbetalingProsent, o.utbetalingProsent) : utbetaltDiff;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            return o instanceof MappedUtbetaling that && utbetalingProsent.equals(that.utbetalingProsent) && utbetaltForDag.equals(that.utbetaltForDag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(utbetalingProsent, utbetaltForDag);
        }
    }

    private static LocalDateTimeline<MappedUtbetaling> mapDatadelingDpsakUtbetaling(List<ArbeidsavklaringspengerResponse.AAPUtbetaling> utbetalinger) {
        var mapped = utbetalinger.stream()
            .map(u -> new LocalDateSegment<>(new LocalDateInterval(u.periode().fraOgMedDato(), u.periode().tilOgMedDato()), new MappedUtbetaling(u)))
            .collect(Collectors.collectingAndThen(Collectors.toList(), LocalDateTimeline::new));
        // Slå sammen dager med lik sats og utbetalingsgrad. Utvide fredager til søndag pga filter over. Summer sumUtbetalt ved sammenslåing
        return mapped.compress(LocalDateInterval::abutsWorkdays, MappedUtbetaling::equals, KelvinMapper::slåSammen);
    }

    private static LocalDateSegment<MappedUtbetaling> slåSammen(LocalDateInterval i,
                                                                            LocalDateSegment<MappedUtbetaling> lhs,
                                                                            LocalDateSegment<MappedUtbetaling> rhs) {
        return new LocalDateSegment<>(i, new MappedUtbetaling(lhs.getValue().utbetalingProsent(), lhs.getValue().utbetaltForDag(),
            lhs.getValue().sumUtbetalt() + rhs.getValue().sumUtbetalt()));
    }
}
