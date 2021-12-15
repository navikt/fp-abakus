package no.nav.foreldrepenger.abakus.vedtak.extract.v1;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import no.nav.abakus.iaygrunnlag.Organisasjon;
import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.Inntektskategori;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.vedtak.ytelse.Aktør;
import no.nav.abakus.vedtak.ytelse.Periode;
import no.nav.abakus.vedtak.ytelse.v1.YtelseV1;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.Anvisning;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.AnvistAndel;
import no.nav.foreldrepenger.abakus.dbstoette.JpaExtension;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseBuilder;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;

public class ExtractFromYtelseV1Test {

    @RegisterExtension
    public static JpaExtension extension = new JpaExtension();

    @Test
    public void skal_lagre_informasjon() {
        VedtakYtelseRepository repository = new VedtakYtelseRepository(extension.getEntityManager());
        ExtractFromYtelseV1 extractor = new ExtractFromYtelseV1(repository);

        YtelseV1 ytelseV1 = new YtelseV1();
        Aktør aktør = new Aktør();
        String aktørId = "1234123412312";
        aktør.setVerdi(aktørId);
        ytelseV1.setAktør(aktør);
        ytelseV1.setVedtakReferanse(UUID.randomUUID().toString());
        ytelseV1.setFagsystem(Fagsystem.FPSAK);
        ytelseV1.setStatus(YtelseStatus.LØPENDE);
        ytelseV1.setType(YtelseType.FORELDREPENGER);
        ytelseV1.setVedtattTidspunkt(LocalDateTime.now());
        Periode periode = new Periode();
        periode.setFom(LocalDate.now().minusDays(30));
        periode.setTom(LocalDate.now().plusDays(30));
        ytelseV1.setPeriode(periode);
        String saksnummer = "99999999999";
        ytelseV1.setSaksnummer(saksnummer);

        Anvisning anvisning = new Anvisning();
        anvisning.setPeriode(periode);
        anvisning.setAndeler(List.of(new AnvistAndel(new Organisasjon("999999999"), 1236, 100, 100, Inntektskategori.ARBEIDSTAKER, "ehuif2897")));
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
}
