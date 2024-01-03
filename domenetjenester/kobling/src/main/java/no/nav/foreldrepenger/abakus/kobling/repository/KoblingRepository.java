package no.nav.foreldrepenger.abakus.kobling.repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.iaygrunnlag.kodeverk.Kodeverdi;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.felles.diff.DiffEntity;
import no.nav.foreldrepenger.abakus.felles.diff.DiffResult;
import no.nav.foreldrepenger.abakus.felles.diff.TraverseGraph;
import no.nav.foreldrepenger.abakus.felles.diff.TraverseJpaEntityGraphConfig;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;

@ApplicationScoped
public class KoblingRepository {
    private static final Logger LOG = LoggerFactory.getLogger(KoblingRepository.class);
    private EntityManager entityManager;

    KoblingRepository() {
        // CDI
    }

    @Inject
    public KoblingRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager");
        this.entityManager = entityManager;
    }

    public Optional<Kobling> hentForKoblingReferanse(KoblingReferanse referanse) {
        return hentForKoblingReferanse(referanse, false);
    }

    public Optional<Kobling> hentForKoblingReferanse(KoblingReferanse referanse, boolean taSkriveLås) {
        TypedQuery<Kobling> query = entityManager.createQuery("FROM Kobling k WHERE koblingReferanse = :referanse", Kobling.class);
        query.setParameter("referanse", referanse);
        if (taSkriveLås) {
            query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        }
        var k = HibernateVerktøy.hentUniktResultat(query);
        validerErAktiv(k);
        return k;
    }

    private void validerErAktiv(Optional<Kobling> k) {
        if (k.isPresent() && !k.get().erAktiv()) {
            throw new IllegalStateException("Etterspør kobling: " + k.get().getKoblingReferanse() + ", men denne er ikke aktiv");
        }
    }

    public Optional<Kobling> hentSisteKoblingReferanseFor(AktørId aktørId, Saksnummer saksnummer, YtelseType ytelseType) {
        TypedQuery<Kobling> query = entityManager.createQuery(
            "FROM Kobling k " + " WHERE k.saksnummer = :ref AND k.ytelseType = :ytelse and k.aktørId = :aktørId and k.aktiv=true" +
                " ORDER BY k.opprettetTidspunkt desc, k.id desc", Kobling.class);
        query.setParameter("ref", saksnummer);
        query.setParameter("ytelse", ytelseType);
        query.setParameter("aktørId", aktørId);
        query.setMaxResults(1);
        var k = query.getResultList().stream().findFirst();
        validerErAktiv(k);
        return k;
    }

    public Optional<Long> hentKoblingIdForKoblingReferanse(KoblingReferanse referanse) {
        var k = hentForKoblingReferanse(referanse);
        return k.map(Kobling::getId);
    }

    public void lagre(Kobling nyKobling) {
        Optional<Kobling> eksisterendeKobling = hentForKoblingReferanse(nyKobling.getKoblingReferanse());

        DiffResult diff = getDiff(eksisterendeKobling.orElse(null), nyKobling);

        if (!diff.isEmpty()) {
            LOG.info("Detekterte endringer på kobling med referanse={}, endringer={}", nyKobling.getId(), diff.getLeafDifferences());
            entityManager.persist(nyKobling);
            entityManager.flush();
        }
    }

    private DiffResult getDiff(Kobling eksisterendeKobling, Kobling nyKobling) {
        var config = new TraverseJpaEntityGraphConfig();
        config.setIgnoreNulls(true);
        config.setOnlyCheckTrackedFields(false);
        config.addLeafClasses(Kodeverdi.class);
        config.addLeafClasses(IntervallEntitet.class);
        var diffEntity = new DiffEntity(new TraverseGraph(config));

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
