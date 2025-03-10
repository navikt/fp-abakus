package no.nav.foreldrepenger.abakus.rydding;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.abakus.dbstoette.JpaExtension;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareTest;

@ExtendWith(JpaExtension.class)
class OppryddingIayInformasjonRepositoryTest extends EntityManagerAwareTest {

    private OppryddingIayInformasjonRepository repository;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        repository = new OppryddingIayInformasjonRepository(entityManager);
    }

    @Test
    void hentIayInformasjonUtenReferanse_medReferanse_ok() {
        opprettEmptyArbeidsforholdInfo();

        var arbeidsforholdInformasjon = repository.hentIayInformasjonUtenReferanse(10);

        assertThat(arbeidsforholdInformasjon).hasSize(1);
    }

    @Test
    void slettIayInformasjon_ok() {
        opprettEmptyArbeidsforholdInfo();

        var arbeidsforholdInformasjon = repository.hentIayInformasjonUtenReferanse(10);
        assertThat(arbeidsforholdInformasjon).hasSize(1);

        repository.slettIayInformasjon(arbeidsforholdInformasjon.getFirst());

        assertThat(repository.hentIayInformasjonUtenReferanse(10)).isEmpty();
    }

    private void opprettEmptyArbeidsforholdInfo() {
        var entity = new ArbeidsforholdInformasjon();
        getEntityManager().persist(entity);
        flushAndClear();
    }

    private void flushAndClear() {
        getEntityManager().flush();
        getEntityManager().clear();
    }
}
