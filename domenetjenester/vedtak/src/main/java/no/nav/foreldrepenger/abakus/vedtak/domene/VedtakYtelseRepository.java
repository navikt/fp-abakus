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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Fagsystem;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;

@ApplicationScoped
public class VedtakYtelseRepository {

    private static final Logger log = LoggerFactory.getLogger(VedtakYtelseRepository.class);
    private EntityManager entityManager;

    VedtakYtelseRepository() {
        // CDI
    }

    @Inject
    public VedtakYtelseRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    public VedtakYtelseBuilder opprettBuilderFor(AktørId aktørId, Saksnummer saksnummer, Fagsystem fagsystem, YtelseType ytelseType) {
        return VedtakYtelseBuilder.oppdatere(hentYtelseFor(aktørId, saksnummer, fagsystem, ytelseType))
            .medAktør(aktørId)
            .medSaksnummer(saksnummer)
            .medKilde(fagsystem)
            .medYtelseType(ytelseType);
    }

    public void lagre(VedtakYtelseBuilder builder) {
        VedtattYtelse ytelse = builder.build();
        Optional<VedtakYtelseEntitet> vedtakYtelseEntitet = hentYtelseFor(ytelse.getAktør(), ytelse.getSaksnummer(), ytelse.getKilde(), ytelse.getYtelseType());
        if (builder.erOppdatering() && vedtakYtelseEntitet.isPresent()) {
            // Deaktiver eksisterende innslag
            VedtakYtelseEntitet ytelseEntitet = vedtakYtelseEntitet.get();
            ytelseEntitet.setAktiv(false);
            entityManager.persist(ytelseEntitet);
            entityManager.flush();
        } else if (!builder.erOppdatering()) {
            ((VedtakYtelseEntitet) ytelse).setAktiv(false);
        }
        if (((VedtakYtelseEntitet) ytelse).getAktiv()) {
            entityManager.persist(ytelse);
            for (YtelseAnvist ytelseAnvist : ytelse.getYtelseAnvist()) {
                entityManager.persist(ytelseAnvist);
            }
            entityManager.flush();
        } else {
            log.info("Forkaster vedtak siden en sitter på nyere vedtak. {} er eldre enn {}", ytelse, vedtakYtelseEntitet);
        }
    }

    private Optional<VedtakYtelseEntitet> hentYtelseFor(AktørId aktørId, Saksnummer saksnummer, Fagsystem fagsystem, YtelseType ytelseType) {
        Objects.requireNonNull(aktørId, "aktørId");
        Objects.requireNonNull(saksnummer, "saksnummer");
        Objects.requireNonNull(fagsystem, "fagsystem");
        Objects.requireNonNull(ytelseType, "ytelseType");

        TypedQuery<VedtakYtelseEntitet> query = entityManager.createQuery("SELECT v FROM VedtakYtelseEntitet v " +
            "WHERE v.aktørId = :aktørId " +
            "AND v.saksnummer = :saksnummer " +
            "AND v.kilde = :fagsystem " +
            "AND v.ytelseType = :ytelse " +
            "AND v.aktiv = true ", VedtakYtelseEntitet.class);
        query.setParameter("aktørId", aktørId);
        query.setParameter("saksnummer", saksnummer);
        query.setParameter("fagsystem", fagsystem);
        query.setParameter("ytelse", ytelseType);

        return HibernateVerktøy.hentUniktResultat(query);
    }

    public List<VedtattYtelse> hentYtelserForIPeriode(AktørId aktørId, LocalDate fom, LocalDate tom) {
        TypedQuery<VedtakYtelseEntitet> query = entityManager.createQuery("FROM VedtakYtelseEntitet " +
            "WHERE aktørId = :aktørId " +
            "AND periode.fomDato <= :tom AND periode.tomDato >= :fom " +
            "AND aktiv = true", VedtakYtelseEntitet.class);
        query.setParameter("aktørId", aktørId);
        query.setParameter("fom", fom);
        query.setParameter("tom", tom);
        query.setHint(QueryHints.HINT_READONLY, true);

        return new ArrayList<>(query.getResultList());
    }
}
