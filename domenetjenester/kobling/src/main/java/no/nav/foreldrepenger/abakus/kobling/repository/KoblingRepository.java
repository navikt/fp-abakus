package no.nav.foreldrepenger.abakus.kobling.repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.felles.diff.DiffEntity;
import no.nav.foreldrepenger.abakus.felles.diff.DiffResult;
import no.nav.foreldrepenger.abakus.felles.diff.TraverseEntityGraph;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kodeverk.Kodeliste;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkTabell;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.jpa.tid.ÅpenDatoIntervallEntitet;

@ApplicationScoped
public class KoblingRepository {
    private static final Logger log = LoggerFactory.getLogger(KoblingRepository.class);
    private EntityManager entityManager;

    KoblingRepository() {
        // CDI
    }

    @Inject
    public KoblingRepository(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    public Optional<Kobling> hentForKoblingReferanse(KoblingReferanse referanse) {
        TypedQuery<Kobling> query = entityManager.createQuery("FROM Kobling k WHERE koblingReferanse = :referanse", Kobling.class);
        query.setParameter("referanse", referanse);
        return HibernateVerktøy.hentUniktResultat(query);
    }

    public Long hentKoblingIdForKoblingReferanse(KoblingReferanse referanse) {
        TypedQuery<Long> query = entityManager.createQuery("SELECT k.id FROM Kobling k WHERE k.koblingReferanse = :referanse", Long.class);
        query.setParameter("referanse", referanse);
        return HibernateVerktøy.hentUniktResultat(query).orElse(null);
    }

    public void lagre(Kobling nyKobling) {
        Optional<Kobling> eksisterendeKobling = hentForKoblingReferanse(nyKobling.getKoblingReferanse());

        DiffResult diff = getDiff(eksisterendeKobling.orElse(null), nyKobling);

        if (!diff.isEmpty()) {
            log.info("Detekterte endringer på kobling med referanse={}, endringer={}", nyKobling.getId(), diff.getLeafDifferences());
            entityManager.persist(nyKobling);
            entityManager.flush();
        }
    }

    private DiffResult getDiff(Kobling eksisterendeKobling, Kobling nyKobling) {
        TraverseEntityGraph traverseEntityGraph = new TraverseEntityGraph(); // NOSONAR
        traverseEntityGraph.setIgnoreNulls(true);
        traverseEntityGraph.setOnlyCheckTrackedFields(false);
        traverseEntityGraph.addLeafClasses(KodeverkTabell.class);
        traverseEntityGraph.addLeafClasses(Kodeliste.class);
        traverseEntityGraph.addLeafClasses(DatoIntervallEntitet.class, ÅpenDatoIntervallEntitet.class);
        DiffEntity diffEntity = new DiffEntity(traverseEntityGraph);

        return diffEntity.diff(eksisterendeKobling, nyKobling);
    }

    public Kobling hentForKoblingId(Long koblingId) {
        return entityManager.find(Kobling.class, koblingId);
    }

    public List<Saksnummer> hentAlleSaksnummer() {
        TypedQuery<Saksnummer> query = entityManager.createQuery("SELECT k.saksnummer FROM Kobling k", Saksnummer.class);
        return query.getResultList();
    }
}
