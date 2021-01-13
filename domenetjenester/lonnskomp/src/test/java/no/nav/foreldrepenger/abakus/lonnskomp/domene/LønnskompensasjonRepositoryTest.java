package no.nav.foreldrepenger.abakus.lonnskomp.domene;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import no.nav.foreldrepenger.abakus.dbstoette.JpaExtension;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;

public class LønnskompensasjonRepositoryTest {

    @RegisterExtension
    public static final JpaExtension repositoryRule = new JpaExtension();

    private static final AktørId AKTØR_ID = new AktørId("1231231231234");
    private static final PersonIdent FNR = new PersonIdent("12312312312");


    private LønnskompensasjonRepository repository = new LønnskompensasjonRepository(repositoryRule.getEntityManager());

    @Test
    public void skal_håndtere_lagring_rett() {
        var anvist = LønnskompensasjonAnvist.LønnskompensasjonAnvistBuilder.ny()
            .medAnvistPeriode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.of(2020,4,20), LocalDate.of(2020,4,20)))
            .medBeløp(new BigDecimal(1000)).build();
        var vedtak = new LønnskompensasjonVedtak();
        vedtak.setAktørId(AKTØR_ID);
        vedtak.setFnr(FNR.getIdent());
        vedtak.setSakId("1234");
        vedtak.setPeriode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusMonths(10), LocalDate.now().minusMonths(9)));
        vedtak.setBeløp(new Beløp(new BigDecimal(10000L)));
        vedtak.setOrgNummer(new OrgNummer("999999999"));
        vedtak.leggTilAnvistPeriode(anvist);

        repository.lagre(vedtak);

        final var vedtakFraRepo = repository.hentLønnskompensasjonForIPeriode(AKTØR_ID, LocalDate.now().minusMonths(17), LocalDate.now());
        assertThat(vedtakFraRepo).hasSize(1);

        final var vedtakForSakId = repository.hentSak("1234", FNR.getIdent());
        assertThat(vedtakForSakId).isPresent();
        assertThat(vedtakForSakId.get().getOrgNummer()).isEqualTo(new OrgNummer("999999999"));

        var nyVedtak = new LønnskompensasjonVedtak(vedtak);
        vedtak.getAnvistePerioder().stream().map(LønnskompensasjonAnvist::new).forEach(nyVedtak::leggTilAnvistPeriode);
        nyVedtak.setBeløp(new Beløp(new BigDecimal(10001L)));

        if (repository.skalLagreVedtak(vedtak, nyVedtak)) {
            repository.lagre(nyVedtak);
        }

        final var oppdatertVedtattVedtak = repository.hentLønnskompensasjonForIPeriode(AKTØR_ID, LocalDate.now().minusMonths(17), LocalDate.now());

        assertThat(oppdatertVedtattVedtak).hasSize(1);
        assertThat(oppdatertVedtattVedtak.get(0).getId()).isNotEqualTo(vedtak.getId());
        assertThat(oppdatertVedtattVedtak.get(0).getBeløp().getVerdi().longValue()).isEqualTo(10001L);
    }

    @Test
    public void skal_forkaste_vedtak_som_likt() {
        var anvist = LønnskompensasjonAnvist.LønnskompensasjonAnvistBuilder.ny()
            .medAnvistPeriode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.of(2020,4,20), LocalDate.of(2020,4,20)))
            .medBeløp(new BigDecimal(1000)).build();
        var vedtak = new LønnskompensasjonVedtak();
        vedtak.setAktørId(AKTØR_ID);
        vedtak.setFnr(FNR.getIdent());
        vedtak.setSakId("1234");
        vedtak.setPeriode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusMonths(10), LocalDate.now().minusMonths(9)));
        vedtak.setBeløp(new Beløp(new BigDecimal(10000L)));
        vedtak.setOrgNummer(new OrgNummer("999999999"));
        vedtak.leggTilAnvistPeriode(anvist);

        repository.lagre(vedtak);

        final var vedtakFraRepo = repository.hentLønnskompensasjonForIPeriode(AKTØR_ID, LocalDate.now().minusYears(1), LocalDate.now());
        assertThat(vedtakFraRepo).hasSize(1);

        var nyVedtak = new LønnskompensasjonVedtak(vedtak);

        if (repository.skalLagreVedtak(vedtak, nyVedtak)) {
            repository.lagre(nyVedtak);
        }

        final var oppdatertVedtattVedtak = repository.hentLønnskompensasjonForIPeriode(AKTØR_ID, LocalDate.now().minusYears(1), LocalDate.now());

        assertThat(oppdatertVedtattVedtak).hasSize(1);
        assertThat(oppdatertVedtattVedtak.get(0).getId()).isEqualTo(vedtak.getId());
    }
}
