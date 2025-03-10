package no.nav.foreldrepenger.abakus.rydding.opptjening;

import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.abakus.dbstoette.JpaExtension;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareTest;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(JpaExtension.class)
class OppryddingIayOppgittOpptjeningRepositoryTest extends EntityManagerAwareTest {

    private OppryddingIayOppgittOpptjeningRepository repository;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        repository = new OppryddingIayOppgittOpptjeningRepository(entityManager);
    }

    @Test
    void hentIayInformasjonUtenReferanse_medReferanse_ok() {
        opprettEmptyOppgittOpptjening();

        var iayOppgittOpptjeningUtenReferanse = repository.hentIayOppgittOpptjeningUtenReferanse(10);

        assertThat(iayOppgittOpptjeningUtenReferanse).hasSize(1);
    }

    @Test
    void slettIayInformasjon_ok() {
        opprettEmptyOppgittOpptjening();

        var iayOppgittOpptjeningUtenReferanse = repository.hentIayOppgittOpptjeningUtenReferanse(10);
        assertThat(iayOppgittOpptjeningUtenReferanse).hasSize(1);

        repository.slettIayOppgittOpptjening(iayOppgittOpptjeningUtenReferanse.getFirst());

        assertThat(repository.hentIayOppgittOpptjeningUtenReferanse(10)).isEmpty();
    }

    private void opprettEmptyOppgittOpptjening() {
        var entity = OppgittOpptjeningBuilder.ny().build();
        getEntityManager().persist(entity);
        flushAndClear();
    }

    private void flushAndClear() {
        getEntityManager().flush();
        getEntityManager().clear();
    }
}
