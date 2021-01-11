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

public class LønnskompensasjonRepositoryTest {

    @RegisterExtension
    public static final JpaExtension repositoryRule = new JpaExtension();

    private LønnskompensasjonRepository repository = new LønnskompensasjonRepository(repositoryRule.getEntityManager());

    @Test
    public void skal_håndtere_lagring_rett() {
        var aktørId = new AktørId("1231231231234");
        var vedtak = new LønnskompensasjonVedtak();
        vedtak.setAktørId(aktørId);
        vedtak.setSakId("1234");
        vedtak.setPeriode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusMonths(10), LocalDate.now().minusMonths(9)));
        vedtak.setBeløp(new Beløp(new BigDecimal(10000L)));
        vedtak.setOrgNummer(new OrgNummer("999999999"));

        repository.lagre(vedtak);

        final var vedtakFraRepo = repository.hentLønnskompensasjonForIPeriode(aktørId, LocalDate.now().minusMonths(17), LocalDate.now());
        assertThat(vedtakFraRepo).hasSize(1);

        final var vedtakForSakId = repository.hentSak("1234");
        assertThat(vedtakForSakId).isPresent();
        assertThat(vedtakForSakId.get().getOrgNummer()).isEqualTo(new OrgNummer("999999999"));

        var nyVedtak = new LønnskompensasjonVedtak(vedtak);
        nyVedtak.setForrigeVedtakDato(LocalDate.now().minusDays(1));
        vedtak.setBeløp(new Beløp(new BigDecimal(10001L)));

        repository.lagre(nyVedtak);

        final var oppdatertVedtattVedtak = repository.hentLønnskompensasjonForIPeriode(aktørId, LocalDate.now().minusMonths(17), LocalDate.now());

        assertThat(oppdatertVedtattVedtak).hasSize(1);
        assertThat(oppdatertVedtattVedtak.get(0).getForrigeVedtakDato()).isNotNull();
    }

    @Test
    public void skal_forkaste_vedtak_som_er_eldre() {
        var aktørId = new AktørId("1231231231234");
        var vedtak = new LønnskompensasjonVedtak();
        vedtak.setAktørId(aktørId);
        vedtak.setSakId("1234");
        vedtak.setPeriode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusMonths(10), LocalDate.now().minusMonths(9)));
        vedtak.setBeløp(new Beløp(new BigDecimal(10000L)));
        vedtak.setOrgNummer(new OrgNummer("999999999"));
        vedtak.setForrigeVedtakDato(LocalDate.now().minusDays(1));

        repository.lagre(vedtak);

        final var vedtakFraRepo = repository.hentLønnskompensasjonForIPeriode(aktørId, LocalDate.now().minusYears(1), LocalDate.now());
        assertThat(vedtakFraRepo).hasSize(1);

        var nyVedtak = new LønnskompensasjonVedtak(vedtak);
        nyVedtak.setForrigeVedtakDato(LocalDate.now().minusDays(10));
        vedtak.setBeløp(new Beløp(new BigDecimal(10001L)));

        repository.lagre(nyVedtak);

        final var oppdatertVedtattVedtak = repository.hentLønnskompensasjonForIPeriode(aktørId, LocalDate.now().minusYears(1), LocalDate.now());

        assertThat(oppdatertVedtattVedtak).hasSize(1);
        assertThat(oppdatertVedtattVedtak.get(0).getForrigeVedtakDato()).isEqualTo(LocalDate.now().minusDays(1));
    }
}
