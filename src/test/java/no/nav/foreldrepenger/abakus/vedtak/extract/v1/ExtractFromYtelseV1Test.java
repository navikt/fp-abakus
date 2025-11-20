package no.nav.foreldrepenger.abakus.vedtak.extract.v1;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.vedtak.ytelse.Aktør;
import no.nav.abakus.vedtak.ytelse.Kildesystem;
import no.nav.abakus.vedtak.ytelse.Periode;
import no.nav.abakus.vedtak.ytelse.Status;
import no.nav.abakus.vedtak.ytelse.Ytelser;
import no.nav.abakus.vedtak.ytelse.v1.YtelseV1;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.Anvisning;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.AnvistAndel;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.ArbeidsgiverIdent;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.Inntektklasse;
import no.nav.foreldrepenger.abakus.dbstoette.JpaExtension;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseBuilder;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;

class ExtractFromYtelseV1Test {

    @RegisterExtension
    public static JpaExtension extension = new JpaExtension();

    @Test
    void skal_lagre_informasjon() {
        VedtakYtelseRepository repository = new VedtakYtelseRepository(extension.getEntityManager());
        ExtractFromYtelseV1 extractor = new ExtractFromYtelseV1(repository);

        YtelseV1 ytelseV1 = new YtelseV1();
        Aktør aktør = new Aktør();
        String aktørId = "1234123412312";
        aktør.setVerdi(aktørId);
        ytelseV1.setAktør(aktør);
        ytelseV1.setVedtakReferanse(UUID.randomUUID().toString());
        ytelseV1.setKildesystem(Kildesystem.FPSAK);
        ytelseV1.setYtelseStatus(Status.LØPENDE);
        ytelseV1.setYtelse(Ytelser.FORELDREPENGER);
        ytelseV1.setVedtattTidspunkt(LocalDateTime.now());
        Periode periode = new Periode();
        periode.setFom(LocalDate.now().minusDays(30));
        periode.setTom(LocalDate.now().plusDays(30));
        ytelseV1.setPeriode(periode);
        String saksnummer = "99999999999";
        ytelseV1.setSaksnummer(saksnummer);

        Anvisning anvisning = new Anvisning();
        anvisning.setPeriode(periode);
        anvisning.setAndeler(List.of(new AnvistAndel(new ArbeidsgiverIdent("999999999"), 1236, 100, 100, Inntektklasse.ARBEIDSTAKER, "ehuif2897")));
        ytelseV1.setAnvist(List.of(anvisning));

        VedtakYtelseBuilder builder = extractor.extractFrom(ytelseV1);
        repository.lagre(builder);
        var entitet = builder.build();

        assertThat(entitet).isNotNull();
        assertThat(entitet.getSaksnummer()).isNotNull();
        assertThat(entitet.getSaksnummer().getVerdi()).isEqualTo(saksnummer);
        assertThat(entitet.getAktør()).isNotNull();
        assertThat(entitet.getAktør().getId()).isEqualTo(aktørId);
        assertThat(entitet.getYtelseAnvist()).hasSize(1);
        assertThat(entitet.getYtelseAnvist().iterator().next().getAndeler()).hasSize(1);
    }

    @Test
    void skal_lagre_informasjon_plain_enum() {
        VedtakYtelseRepository repository = new VedtakYtelseRepository(extension.getEntityManager());
        ExtractFromYtelseV1 extractor = new ExtractFromYtelseV1(repository);

        YtelseV1 ytelseV1 = new YtelseV1();
        Aktør aktør = new Aktør();
        String aktørId = "1234123412312";
        aktør.setVerdi(aktørId);
        ytelseV1.setAktør(aktør);
        ytelseV1.setVedtakReferanse(UUID.randomUUID().toString());
        ytelseV1.setKildesystem(Kildesystem.FPSAK);
        ytelseV1.setYtelseStatus(Status.LØPENDE);
        ytelseV1.setYtelse(Ytelser.FORELDREPENGER);
        ytelseV1.setVedtattTidspunkt(LocalDateTime.now());
        Periode periode = new Periode();
        periode.setFom(LocalDate.now().minusDays(30));
        periode.setTom(LocalDate.now().plusDays(30));
        ytelseV1.setPeriode(periode);
        String saksnummer = "99999999999";
        ytelseV1.setSaksnummer(saksnummer);

        Anvisning anvisning = new Anvisning();
        anvisning.setPeriode(periode);
        anvisning.setAndeler(List.of(new AnvistAndel(new ArbeidsgiverIdent("999999999"), 1236, 100, 100, Inntektklasse.ARBEIDSTAKER, "ehuif2897")));
        ytelseV1.setAnvist(List.of(anvisning));

        VedtakYtelseBuilder builder = extractor.extractFrom(ytelseV1);
        repository.lagre(builder);
        var entitet = builder.build();

        assertThat(entitet).isNotNull();
        assertThat(entitet.getSaksnummer()).isNotNull();
        assertThat(entitet.getSaksnummer().getVerdi()).isEqualTo(saksnummer);
        assertThat(entitet.getAktør()).isNotNull();
        assertThat(entitet.getAktør().getId()).isEqualTo(aktørId);
        assertThat(entitet.getYtelseAnvist()).hasSize(1);
        assertThat(entitet.getYtelseAnvist().iterator().next().getAndeler()).hasSize(1);
        assertThat(entitet.getKilde()).isEqualTo(Fagsystem.FPSAK);
        assertThat(entitet.getYtelseType()).isEqualTo(YtelseType.FORELDREPENGER);
        assertThat(entitet.getStatus()).isEqualTo(YtelseStatus.LØPENDE);
    }
}
