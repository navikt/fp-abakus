package no.nav.foreldrepenger.abakus.vedtak.domene;

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

import no.nav.foreldrepenger.abakus.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Fagsystem;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class VedtakYtelseRepository {

    private EntityManager entityManager;

    VedtakYtelseRepository() {
        // CDI
    }

    @Inject
    public VedtakYtelseRepository(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    public VedtakYtelseBuilder opprettBuilderFor(AktørId aktørId, Saksnummer saksnummer, Fagsystem fagsystem, RelatertYtelseType ytelseType) {
        return VedtakYtelseBuilder.oppdatere(hentYtelseFor(aktørId, saksnummer, fagsystem, ytelseType))
            .medAktør(aktørId)
            .medSaksnummer(saksnummer)
            .medKilde(fagsystem)
            .medYtelseType(ytelseType);
    }

    public void lagre(VedtakYtelseBuilder builder) {
        if (builder.erOppdatering()) {
            VedtattYtelse ytelse = builder.build();
            entityManager.persist(ytelse);
        }
    }

    private Optional<VedtakYtelseEntitet> hentYtelseFor(AktørId aktørId, Saksnummer saksnummer, Fagsystem fagsystem, RelatertYtelseType ytelseType) {
        Objects.requireNonNull(aktørId, "aktørId");
        Objects.requireNonNull(saksnummer, "saksnummer");
        Objects.requireNonNull(fagsystem, "fagsystem");
        Objects.requireNonNull(ytelseType, "ytelseType");

        TypedQuery<VedtakYtelseEntitet> query = entityManager.createQuery("FROM VedtakYtelseEntitet " +
            "WHERE aktørId = :aktørId " +
            "AND saksnummer = :saksnummer " +
            "AND kilde = :fagsystem " +
            "AND ytelseType = :ytelse", VedtakYtelseEntitet.class);

        query.setParameter("aktørId", aktørId);
        query.setParameter("saksnummer", saksnummer);
        query.setParameter("fagsystem", fagsystem);
        query.setParameter("ytelse", ytelseType);

        return HibernateVerktøy.hentUniktResultat(query);
    }

    public List<VedtattYtelse> hentYtelserForIPeriode(AktørId aktørId, LocalDate fom, LocalDate tom) {
        TypedQuery<VedtakYtelseEntitet> query = entityManager.createQuery("FROM VedtakYtelseEntitet " +
            "WHERE aktørId = :aktørId " +
            "AND periode.fomDato <= :tom AND periode.tomDato >= :fom", VedtakYtelseEntitet.class);
        query.setParameter("aktørId", aktørId);
        query.setParameter("fom", fom);
        query.setParameter("tom", tom);
        query.setHint(QueryHints.HINT_READONLY, true);

        return new ArrayList<>(query.getResultList());
    }
}
