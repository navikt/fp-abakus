package no.nav.foreldrepenger.abakus.rydding;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.abakus.dbstoette.JpaExtension;
import no.nav.foreldrepenger.abakus.domene.iay.InntektsmeldingAggregat;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareTest;

@ExtendWith(JpaExtension.class)
class OppryddingIayInntektsmeldingerRepositoryTest extends EntityManagerAwareTest {

    private OppryddingIayInntektsmeldingerRepository repository;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        repository = new OppryddingIayInntektsmeldingerRepository(entityManager);
    }

    @Test
    void hentIayInformasjonUtenReferanse_medReferanse_ok() {
        opprettEmptyArbeidsforholdInfo();

        var iayInntektsmeldingerUtenReferanse = repository.hentIayInntektsmeldingerUtenReferanse(10);

        assertThat(iayInntektsmeldingerUtenReferanse).hasSize(1);
    }

    @Test
    void slettIayInformasjon_ok() {
        opprettEmptyArbeidsforholdInfo();

        var iayInntektsmeldingerUtenReferanse = repository.hentIayInntektsmeldingerUtenReferanse(10);
        assertThat(iayInntektsmeldingerUtenReferanse).hasSize(1);

        repository.slettIayInntektsmeldinger(iayInntektsmeldingerUtenReferanse.getFirst());

        assertThat(repository.hentIayInntektsmeldingerUtenReferanse(10)).isEmpty();
    }

    private void opprettEmptyArbeidsforholdInfo() {
        var entity = new InntektsmeldingAggregat();
        getEntityManager().persist(entity);
        flushAndClear();
    }

    private void flushAndClear() {
        getEntityManager().flush();
        getEntityManager().clear();
    }
}
