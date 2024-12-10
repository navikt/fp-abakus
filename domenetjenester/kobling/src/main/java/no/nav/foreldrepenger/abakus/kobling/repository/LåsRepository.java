package no.nav.foreldrepenger.abakus.kobling.repository;

import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingLås;
import no.nav.vedtak.exception.TekniskException;

@ApplicationScoped
public class LåsRepository {
    private EntityManager entityManager;

    LåsRepository() {
        // CDI
    }

    @Inject
    public LåsRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager");
        this.entityManager = entityManager;
    }

    /**
     * Initialiser lås og ta lock på tilhørende database rader.
     */
    public KoblingLås taLås(Long koblingId) {
        if (koblingId != null) {
            entityManager.createQuery("from Kobling k where k.id = :id and k.aktiv = true")
                .setParameter("id", koblingId)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getSingleResult();
        }
        return new KoblingLås(koblingId);
    }

    /**
     * Verifiser lås ved å sjekke mot underliggende lager.
     */
    public void oppdaterLåsVersjon(KoblingLås lås) {
        if (lås.koblingId() != null) {
            var koblingId = lås.koblingId();
            var kobling = entityManager.find(Kobling.class, koblingId);
            if (kobling == null) {
                throw new TekniskException("FP-131239", String.format("Fant ikke entitet for låsing [%s], koblingId=%s.", Kobling.class.getSimpleName(), koblingId));
            } else {
                entityManager.lock(kobling, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
            }
        }
    }

}
