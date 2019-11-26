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
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;
import no.nav.vedtak.konfig.Tid;

@ApplicationScoped
public class VedtakYtelseRepository {

    private static final Logger log = LoggerFactory.getLogger(VedtakYtelseRepository.class);
    private EntityManager entityManager;

    VedtakYtelseRepository() {
        // CDI
    }

    @Inject
    public VedtakYtelseRepository(@VLPersistenceUnit EntityManager entityManager) {
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
        } else if(builder.erOppdatering()) {
            ((VedtakYtelseEntitet)ytelse).setAktiv(false);
        }
        if (!vedtakYtelseEntitet.map(VedtakYtelseEntitet::getAktiv).orElse(false)) {
            entityManager.persist(ytelse);
            for (YtelseAnvist ytelseAnvist : ytelse.getYtelseAnvist()) {
                entityManager.persist(ytelseAnvist);
            }
            entityManager.flush();
        } else {
            log.info("Forkaster vedtak siden en sitter på nyere vedtak. {} er eldre enn {}", ytelse, vedtakYtelseEntitet.get());
        }
    }

    public void lagre(VedtakYtelseBuilder builder, Optional<VedtakYtelseEntitet> siste) {
        VedtakYtelseEntitet ytelse = (VedtakYtelseEntitet)builder.build();
        if (ytelse.getVedtattTidspunkt().isBefore(siste.map(VedtakYtelseEntitet::getVedtattTidspunkt).orElse(Tid.TIDENES_BEGYNNELSE.atStartOfDay()))) {
            log.info("Forkaster vedtak siden en sitter på nyere vedtak. {} er eldre enn {}", ytelse, siste.get());
            return;
        }
        siste.ifPresent(vedtak -> {
            vedtak.setAktiv(false);
            entityManager.persist(vedtak);
            entityManager.flush();
        });

        entityManager.persist(ytelse);
        for (YtelseAnvist ytelseAnvist : ytelse.getYtelseAnvist()) {
            entityManager.persist(ytelseAnvist);
        }
        entityManager.flush();
    }

    public Optional<VedtakYtelseEntitet> hentYtelseFor(AktørId aktørId, Saksnummer saksnummer, Fagsystem fagsystem, YtelseType ytelseType) {
        Objects.requireNonNull(aktørId, "aktørId");
        Objects.requireNonNull(saksnummer, "saksnummer");
        Objects.requireNonNull(fagsystem, "fagsystem");
        Objects.requireNonNull(ytelseType, "ytelseType");

        TypedQuery<VedtakYtelseEntitet> query = entityManager.createQuery("FROM VedtakYtelseEntitet " +
            "WHERE aktørId = :aktørId " +
            "AND saksnummer = :saksnummer " +
            "AND kilde = :fagsystem " +
            "AND ytelseType = :ytelse " +
            "AND aktiv = true", VedtakYtelseEntitet.class);

        query.setParameter("aktørId", aktørId);
        query.setParameter("saksnummer", saksnummer);
        query.setParameter("fagsystem", fagsystem);
        query.setParameter("ytelse", ytelseType);

        return HibernateVerktøy.hentUniktResultat(query);
    }

    public Optional<VedtakYtelseEntitet> hentSisteYtelseFor(AktørId aktørId, Saksnummer saksnummer, Fagsystem fagsystem, YtelseType ytelseType) {
        Objects.requireNonNull(aktørId, "aktørId");
        Objects.requireNonNull(saksnummer, "saksnummer");
        Objects.requireNonNull(fagsystem, "fagsystem");
        Objects.requireNonNull(ytelseType, "ytelseType");

        TypedQuery<VedtakYtelseEntitet> query = entityManager.createQuery("FROM VedtakYtelseEntitet " +
            "WHERE aktørId = :aktørId " +
            "AND saksnummer = :saksnummer " +
            "AND kilde = :fagsystem " +
            "AND ytelseType = :ytelse " +
            "ORDER BY vedtatt_tidspunkt DESC", VedtakYtelseEntitet.class);

        query.setParameter("aktørId", aktørId);
        query.setParameter("saksnummer", saksnummer);
        query.setParameter("fagsystem", fagsystem);
        query.setParameter("ytelse", ytelseType);

        return optionalFirst(query.getResultList());
    }

    private static Optional<VedtakYtelseEntitet> optionalFirst(List<VedtakYtelseEntitet> vedtak) {
        return vedtak.isEmpty() ? Optional.empty() : Optional.of(vedtak.get(0));
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
