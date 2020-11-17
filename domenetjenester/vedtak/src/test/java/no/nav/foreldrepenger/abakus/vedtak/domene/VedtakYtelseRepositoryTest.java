package no.nav.foreldrepenger.abakus.vedtak.domene;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.dbstoette.JpaExtension;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;

public class VedtakYtelseRepositoryTest {

    @RegisterExtension
    public static JpaExtension repositoryRule = new JpaExtension();
  
    private VedtakYtelseRepository repository = new VedtakYtelseRepository(repositoryRule.getEntityManager());

    @Test
    public void skal_håndtere_lagring_rett() {
        final var aktørId = new AktørId("1231231231234");
        final var saksnummer = new Saksnummer("1234");
        final var builder = repository.opprettBuilderFor(aktørId, saksnummer, Fagsystem.FPSAK, YtelseType.FORELDREPENGER);

        builder.medPeriode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusYears(1), LocalDate.now()))
            .medStatus(YtelseStatus.LØPENDE)
            .medVedtakReferanse(UUID.randomUUID())
            .medVedtattTidspunkt(LocalDateTime.now());

        repository.lagre(builder);

        final var vedtattYtelser = repository.hentYtelserForIPeriode(aktørId, LocalDate.now().minusDays(30), LocalDate.now());
        assertThat(vedtattYtelser).hasSize(1);

        final var nyBuilder = repository.opprettBuilderFor(aktørId, saksnummer, Fagsystem.FPSAK, YtelseType.FORELDREPENGER);
        final var intervallEntitet = IntervallEntitet.fraOgMedTilOgMed(LocalDate.now(), LocalDate.now().plusMonths(3));
        nyBuilder.medVedtakReferanse(UUID.randomUUID())
            .medVedtattTidspunkt(LocalDateTime.now())
            .medPeriode(intervallEntitet);

        repository.lagre(nyBuilder);

        final var oppdatertVedtattYtelser = repository.hentYtelserForIPeriode(aktørId, LocalDate.now().minusDays(30), LocalDate.now());

        assertThat(oppdatertVedtattYtelser).hasSize(1);
        final var vedtattYtelse = oppdatertVedtattYtelser.get(0);
        assertThat(vedtattYtelse.getPeriode()).isEqualTo(intervallEntitet);
    }

    @Test
    public void skal_forkaste_vedtak_som_er_eldre() {
        final var aktørId = new AktørId("1231231231234");
        final var saksnummer = new Saksnummer("1234");
        final var builder = repository.opprettBuilderFor(aktørId, saksnummer, Fagsystem.FPSAK, YtelseType.FORELDREPENGER);

        builder.medPeriode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusYears(1), LocalDate.now()))
            .medStatus(YtelseStatus.LØPENDE)
            .medVedtakReferanse(UUID.randomUUID())
            .medVedtattTidspunkt(LocalDateTime.now());

        repository.lagre(builder);

        final var vedtattYtelser = repository.hentYtelserForIPeriode(aktørId, LocalDate.now().minusDays(30), LocalDate.now());
        assertThat(vedtattYtelser).hasSize(1);

        final var nyBuilder = repository.opprettBuilderFor(aktørId, saksnummer, Fagsystem.FPSAK, YtelseType.FORELDREPENGER);
        final var intervallEntitet = IntervallEntitet.fraOgMedTilOgMed(LocalDate.now(), LocalDate.now().plusMonths(3));
        nyBuilder.medVedtakReferanse(UUID.randomUUID())
            .medVedtattTidspunkt(LocalDateTime.now().minusMinutes(10))
            .medStatus(YtelseStatus.LØPENDE)
            .medPeriode(intervallEntitet);

        repository.lagre(nyBuilder);

        final var oppdatertVedtattYtelser = repository.hentYtelserForIPeriode(aktørId, LocalDate.now().minusDays(30), LocalDate.now());

        assertThat(oppdatertVedtattYtelser).hasSize(1);
        assertThat(oppdatertVedtattYtelser).hasSameElementsAs(vedtattYtelser);
    }
}
