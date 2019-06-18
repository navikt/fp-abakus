package no.nav.foreldrepenger.abakus.vedtak.extract.v1;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.abakus.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseBuilder;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtattYtelse;
import no.nav.vedtak.ytelse.Aktør;
import no.nav.vedtak.ytelse.Desimaltall;
import no.nav.vedtak.ytelse.Periode;
import no.nav.vedtak.ytelse.v1.Fagsystem;
import no.nav.vedtak.ytelse.v1.YtelseStatus;
import no.nav.vedtak.ytelse.v1.YtelseType;
import no.nav.vedtak.ytelse.v1.YtelseV1;
import no.nav.vedtak.ytelse.v1.anvisning.Anvisning;

public class ExtractFromYtelseV1Test {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repositoryRule.getEntityManager());
    private VedtakYtelseRepository repository = new VedtakYtelseRepository(repositoryRule.getEntityManager());
    private ExtractFromYtelseV1 extractor = new ExtractFromYtelseV1(repository, kodeverkRepository);

    @Test
    public void skal_lagre_informasjon() {
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
        anvisning.setBeløp(new Desimaltall(BigDecimal.TEN));
        anvisning.setDagsats(new Desimaltall(BigDecimal.TEN));
        anvisning.setUtbetalingsgrad(new Desimaltall(BigDecimal.TEN));
        anvisning.setPeriode(periode);
        ytelseV1.setAnvist(List.of(anvisning));

        VedtakYtelseBuilder builder = extractor.extractFrom(ytelseV1);
        repository.lagre(builder);
        VedtattYtelse entitet = builder.build();


        assertThat(entitet).isNotNull();
        assertThat(entitet.getSaksnummer()).isNotNull();
        assertThat(entitet.getSaksnummer().getVerdi()).isEqualTo(saksnummer);
        assertThat(entitet.getAktør()).isNotNull();
        assertThat(entitet.getAktør().getId()).isEqualTo(aktørId);
        assertThat(entitet.getYtelseAnvist()).hasSize(1);
    }
}
