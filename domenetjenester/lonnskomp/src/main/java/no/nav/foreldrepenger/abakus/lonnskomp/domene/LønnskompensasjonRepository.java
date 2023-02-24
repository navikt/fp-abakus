package no.nav.foreldrepenger.abakus.lonnskomp.domene;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;

import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;

@ApplicationScoped
public class LønnskompensasjonRepository {

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
        LønnskompensasjonVedtak eksisterende = hentSak(vedtak.getSakId(), vedtak.getFnr()).orElse(null);
        if (eksisterende != null) {
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

    public Optional<LønnskompensasjonVedtak> hentSak(String sakId, String fnr) {
        Objects.requireNonNull(sakId, "sakId");

        TypedQuery<LønnskompensasjonVedtak> query = entityManager.createQuery(
            "SELECT v FROM LonnskompVedtakEntitet v " + "WHERE aktiv = true AND v.sakId = :sakId and v.fnr = :fnr", LønnskompensasjonVedtak.class);
        query.setParameter("sakId", sakId);
        query.setParameter("fnr", fnr);

        return HibernateVerktøy.hentUniktResultat(query);
    }

    public Set<LønnskompensasjonVedtak> hentLønnskompensasjonForIPeriode(AktørId aktørId, LocalDate fom, LocalDate tom) {
        TypedQuery<LønnskompensasjonVedtak> query = entityManager.createQuery(
            "FROM LonnskompVedtakEntitet " + "WHERE aktørId = :aktørId " + "AND periode.fomDato <= :tom AND periode.tomDato >= :fom "
                + "AND aktiv = true", LønnskompensasjonVedtak.class);
        query.setParameter("aktørId", aktørId);
        query.setParameter("fom", fom);
        query.setParameter("tom", tom);
        query.setHint(QueryHints.HINT_READONLY, true);

        Set<LønnskompensasjonVedtak> resultat = new LinkedHashSet<>();
        var allevedtak = query.getResultList();
        for (LønnskompensasjonVedtak v : allevedtak) {
            if (resultat.stream().noneMatch(e -> LønnskompensasjonVedtak.erLikForBrukerOrg(e, v))) {
                resultat.add(v);
            }
        }
        return resultat;
    }

    public boolean skalLagreVedtak(LønnskompensasjonVedtak eksisterende, LønnskompensasjonVedtak vedtak) {
        if (vedtak == null) {
            return false;
        }
        if (eksisterende == null) {
            return true;
        }
        var likeUtenomForrigeVedtak = Objects.equals(eksisterende, vedtak);
        return !likeUtenomForrigeVedtak;
    }

}
