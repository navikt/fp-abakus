package no.nav.foreldrepenger.abakus.registerdata.ytelse.kelvin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagMeldekort;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.konfig.Tid;

public class KelvinMapper {
    private static final Logger LOG = LoggerFactory.getLogger(KelvinMapper.class);

    private KelvinMapper() {
    }

    static List<MeldekortUtbetalingsgrunnlagSak> mapTilMeldekortAclKelvin(List<ArbeidsavklaringspengerResponse.AAPVedtak> vedtak, Saksnummer saksnummer) {
        var mapped = vedtak.stream()
            .map(KelvinMapper::mapTilMeldekortSakAclKelvin)
            .sorted(Comparator.comparing(MeldekortUtbetalingsgrunnlagSak::getVedtaksPeriodeFom))
            .toList();
        if (!mapped.isEmpty()) {
            LOG.info("Kelvin-saker Kelvin for sak {} kilde {} mapped {}.", saksnummer.getVerdi(), vedtak, mapped);
        }
        return mapped;
    }

    private static MeldekortUtbetalingsgrunnlagSak mapTilMeldekortSakAclKelvin(ArbeidsavklaringspengerResponse.AAPVedtak vedtak) {
        var aktuellDagsats = Optional.ofNullable(vedtak.dagsatsEtterUføreReduksjon()).orElseGet(vedtak::dagsats);
        var vedtaksdagsatsMedBarnetillegg = aktuellDagsats + Optional.ofNullable(vedtak.barnetillegg()).orElse(0);
        var mk = vedtak.utbetaling().stream()
            .map(u -> KelvinMapper.mapTilMeldekortMKAclKelvin(u, aktuellDagsats, vedtak.dagsats()))
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
            .medVedtaksDagsats(BigDecimal.valueOf(vedtaksdagsatsMedBarnetillegg))
            .build();
    }

    private static MeldekortUtbetalingsgrunnlagMeldekort mapTilMeldekortMKAclKelvin(ArbeidsavklaringspengerResponse.AAPUtbetaling utbetaling,
                                                                                    Integer aktuellDagsats, Integer vedtakDagsats) {
        // Her vil tilfelle med uførereduksjon ha en ubetalingsgrad mellom 0 og 100 gitt av uførereduksjonen + aktivitet i perioden
        // Gjør derfor en normalisering slik at bruker med 60% AAP får utbetalingsgrad 100% ved full AAP-utbetaling uten aktivitet
        var utbetalingsgradFraUtbetaling = Optional.ofNullable(utbetaling.utbetalingsgrad()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO);
        var brukUtbetalingsgrad = utbetalingsgradFraUtbetaling
            .multiply(BigDecimal.valueOf(vedtakDagsats))
            .divide(BigDecimal.valueOf(aktuellDagsats), 0, RoundingMode.HALF_EVEN);
        var dagsats = Optional.ofNullable(utbetaling.dagsats()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO)
            .add(Optional.ofNullable(utbetaling.barnetillegg()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO));
        return MeldekortUtbetalingsgrunnlagMeldekort.MeldekortMeldekortBuilder.ny()
            .medMeldekortFom(Tid.fomEllerMin(utbetaling.periode().fraOgMedDato()))
            .medMeldekortTom(Tid.tomEllerMax(utbetaling.periode().tilOgMedDato()))
            .medBeløp(Optional.ofNullable(utbetaling.belop()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO))
            .medDagsats(dagsats)
            .medUtbetalingsgrad(brukUtbetalingsgrad)
            .build();
    }
}
