package no.nav.foreldrepenger.abakus.kobling.repository;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
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
        var sql = "FROM Kobling k WHERE koblingReferanse = :referanse";
        if (taSkriveLås) {
            sql += " and k.aktiv = true";
        }
        var query = entityManager.createQuery(sql, Kobling.class);
        query.setParameter("referanse", referanse);
        if (taSkriveLås) {
            query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        }
        return HibernateVerktøy.hentUniktResultat(query);
    }

    public Optional<Kobling> hentSisteKoblingReferanseFor(AktørId aktørId, Saksnummer saksnummer, YtelseType ytelseType) {
        var query = entityManager.createQuery("""
            FROM Kobling k
            WHERE k.saksnummer = :ref
                AND k.ytelseType = :ytelse
                AND k.aktørId = :aktørId
            ORDER BY k.opprettetTidspunkt desc, k.id desc""", Kobling.class);

        query.setParameter("ref", saksnummer);
        query.setParameter("ytelse", ytelseType);
        query.setParameter("aktørId", aktørId);
        query.setMaxResults(1);
        return query.getResultList().stream().findFirst();
    }

    public void lagre(Kobling nyKobling) {
        // om nyKobling er persistert fra tidligere (har id != null) vil alltid eksisterendeKobling være likt nyKobling (med alle de endringene som er gjort til nyKobling underveis)
        var eksisterendeKobling = hentForKoblingReferanse(nyKobling.getKoblingReferanse());

        validerLikKobling(nyKobling, eksisterendeKobling);

        var diff = getDiff(eksisterendeKobling.orElse(null), nyKobling);
        // Diffen blir aldri forskjellig om nyKobling er allerede persistert i databasen. Men endringen blir skrevet til databasen likevel da hele transaksjonen commites.
        if (!diff.isEmpty()) {
            if (nyKobling.getId() == null) {
                LOG.info("KOBLING: Lagrer en helt ny kobling med id={}, endringer={}", nyKobling.getId(), diff.getLeafDifferences());
            } else {
                LOG.info("KOBLING: Lagrer endringer på kobling med id={}, endringer={}", nyKobling.getId(), diff.getLeafDifferences());
            }
            LOG.info("KOBLING: Detekterte endringer på kobling med id={}, endringer={}", nyKobling.getId(), diff.getLeafDifferences());
            entityManager.persist(nyKobling);
            entityManager.flush();
        } else {
            LOG.info("KOBLING: Ingen endringer på kobling med id={}", nyKobling.getId());
        }
    }

    private static void validerLikKobling(Kobling nyKobling, Optional<Kobling> eksisterendeKobling) {
        var eksisterendeKoblingId = eksisterendeKobling.map(Kobling::getId).orElse(null);
        if (!Objects.equals(eksisterendeKoblingId, nyKobling.getId())) { // for nye koblinger bør både eksisterende og ny være null.
            throw new IllegalStateException("Utviklerfeil: Kan ikke lagre en ny kobling for eksisterende kobling referanse.");
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

}
