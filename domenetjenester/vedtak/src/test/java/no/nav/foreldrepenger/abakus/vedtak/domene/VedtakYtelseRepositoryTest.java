package no.nav.foreldrepenger.abakus.vedtak.domene;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.abakus.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Fagsystem;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class VedtakYtelseRepositoryTest {

    @Rule
    public RepositoryRule repositoryRule = new UnittestRepositoryRule();

    private VedtakYtelseRepository repository = new VedtakYtelseRepository(repositoryRule.getEntityManager());

    @Test
    public void skal_håndtere_lagring_rett() {
        final var aktørId = new AktørId("1231231231234");
        final var saksnummer = new Saksnummer("1234");
        final var builder = repository.opprettBuilderFor(aktørId, saksnummer, Fagsystem.FPSAK, YtelseType.FORELDREPENGER);

        builder.medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusYears(1), LocalDate.now()))
            .medStatus(YtelseStatus.LØPENDE)
            .medVedtakReferanse(UUID.randomUUID())
            .medVedtattTidspunkt(LocalDateTime.now());

        repository.lagre(builder);

        final var vedtattYtelser = repository.hentYtelserForIPeriode(aktørId, LocalDate.now().minusDays(30), LocalDate.now());
        assertThat(vedtattYtelser).hasSize(1);

        final var nyBuilder = repository.opprettBuilderFor(aktørId, saksnummer, Fagsystem.FPSAK, YtelseType.FORELDREPENGER);
        final var intervallEntitet = DatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.now(), LocalDate.now().plusMonths(3));
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

        builder.medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusYears(1), LocalDate.now()))
            .medStatus(YtelseStatus.LØPENDE)
            .medVedtakReferanse(UUID.randomUUID())
            .medVedtattTidspunkt(LocalDateTime.now());

        repository.lagre(builder);

        final var vedtattYtelser = repository.hentYtelserForIPeriode(aktørId, LocalDate.now().minusDays(30), LocalDate.now());
        assertThat(vedtattYtelser).hasSize(1);

        final var nyBuilder = repository.opprettBuilderFor(aktørId, saksnummer, Fagsystem.FPSAK, YtelseType.FORELDREPENGER);
        final var intervallEntitet = DatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.now(), LocalDate.now().plusMonths(3));
        nyBuilder.medVedtakReferanse(UUID.randomUUID())
            .medVedtattTidspunkt(LocalDateTime.now().minusMinutes(10))
            .medPeriode(intervallEntitet);

        repository.lagre(nyBuilder);

        final var oppdatertVedtattYtelser = repository.hentYtelserForIPeriode(aktørId, LocalDate.now().minusDays(30), LocalDate.now());

        assertThat(oppdatertVedtattYtelser).hasSize(1);
        assertThat(oppdatertVedtattYtelser).hasSameElementsAs(vedtattYtelser);
    }
}
