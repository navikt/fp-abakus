package no.nav.foreldrepenger.abakus.lonnskomp.domene;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;

@ApplicationScoped
public class LønnskompensasjonRepository {

    private static final Logger log = LoggerFactory.getLogger(LønnskompensasjonRepository.class);
    private EntityManager entityManager;

    LønnskompensasjonRepository() {
        // CDI
    }

    @Inject
    public LønnskompensasjonRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    public void lagre(LønnskompensasjonVedtak vedtak) {
        LønnskompensasjonVedtak eksisterende = hentSak(vedtak.getSakId()).orElse(null);
        if (eksisterende != null) {
            if ((eksisterende.getForrigeVedtakDato() == null && vedtak.getForrigeVedtakDato() == null) ||
                (eksisterende.getForrigeVedtakDato() != null && (vedtak.getForrigeVedtakDato() == null || !vedtak.getForrigeVedtakDato().isAfter(eksisterende.getForrigeVedtakDato())))) {
                log.info("Forkaster lønnskompensasjon siden en sitter på nyere vedtak. {} er eldre enn {}", vedtak, eksisterende);
                return;
            }
            // Deaktiver eksisterende innslag
            eksisterende.setAktiv(false);
            entityManager.persist(eksisterende);
            entityManager.flush();
        }
        entityManager.persist(vedtak);
        for (LønnskompensasjonAnvist anvist : vedtak.getAnvistePerioder()) {
            entityManager.persist(anvist);
        }
        entityManager.flush();
    }

    public Optional<LønnskompensasjonVedtak> hentSak(String sakId) {
        Objects.requireNonNull(sakId, "sakId");

        TypedQuery<LønnskompensasjonVedtak> query = entityManager.createQuery("SELECT v FROM LonnskompVedtakEntitet v " +
            "WHERE aktiv = true AND v.sakId = :sakId ", LønnskompensasjonVedtak.class);
        query.setParameter("sakId", sakId);

        return HibernateVerktøy.hentUniktResultat(query);
    }

    public List<LønnskompensasjonVedtak> hentLønnskompensasjonForIPeriode(AktørId aktørId, LocalDate fom, LocalDate tom) {
        TypedQuery<LønnskompensasjonVedtak> query = entityManager.createQuery("FROM LonnskompVedtakEntitet " +
            "WHERE aktørId = :aktørId " +
            "AND periode.fomDato <= :tom AND periode.tomDato >= :fom " +
            "AND aktiv = true", LønnskompensasjonVedtak.class);
        query.setParameter("aktørId", aktørId);
        query.setParameter("fom", fom);
        query.setParameter("tom", tom);
        query.setHint(QueryHints.HINT_READONLY, true);

        return new ArrayList<>(query.getResultList());
    }

    public void oppdaterFødselsnummer(String fnr, AktørId aktørId) {
        Objects.requireNonNull(fnr, "fnr");

        entityManager.createNativeQuery("UPDATE lonnskomp_vedtak SET aktoer_id = :aid WHERE fnr = :fnr")
            .setParameter("aid", aktørId.getId()).setParameter("fnr", fnr).executeUpdate();
        entityManager.flush();
    }
}
