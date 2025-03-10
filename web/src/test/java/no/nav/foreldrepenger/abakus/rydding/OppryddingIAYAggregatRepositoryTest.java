package no.nav.foreldrepenger.abakus.rydding;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.abakus.dbstoette.JpaExtension;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.VersjonType;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareTest;

@ExtendWith(JpaExtension.class)
class OppryddingIAYAggregatRepositoryTest extends EntityManagerAwareTest {

    private OppryddingIAYAggregatRepository repository;

    @BeforeEach
    void setUp(EntityManager entityManager) {
        repository = new OppryddingIAYAggregatRepository(entityManager);
    }

    @Test
    void hentIayAggregaterUtenReferanse_medReferanse_ok() {
        opprettEmptyIayAggregat(VersjonType.REGISTER);

        var iayAggregater = repository.hentIayAggregaterUtenReferanse(250);

        assertThat(iayAggregater).hasSize(1);
    }

    @Test
    void slettIayAggregat_register_ok() {
        opprettEmptyIayAggregat(VersjonType.REGISTER);

        var iayAggregater = repository.hentIayAggregaterUtenReferanse(250);
        assertThat(iayAggregater).hasSize(1);

        repository.slettIayAggregat(iayAggregater.getFirst());

        assertThat(repository.hentIayAggregaterUtenReferanse(250)).isEmpty();
    }

    @Test
    void slettIayAggregat_saksbehandlet_ok() {
        opprettEmptyIayAggregat(VersjonType.SAKSBEHANDLET);

        var iayAggregater = repository.hentIayAggregaterUtenReferanse(250);
        assertThat(iayAggregater).hasSize(1);

        repository.slettIayAggregat(iayAggregater.getFirst());

        assertThat(repository.hentIayAggregaterUtenReferanse(250)).isEmpty();
    }

    private void opprettEmptyIayAggregat(VersjonType versjonType) {
        var iayAggregat = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), versjonType).build();
        getEntityManager().persist(iayAggregat);
        flushAndClear();
    }

    private void flushAndClear() {
        getEntityManager().flush();
        getEntityManager().clear();
    }
}
