package no.nav.foreldrepenger.abakus.domene.virksomhet;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class VirksomhetRepositoryImpl implements VirksomhetRepository {

    private EntityManager entityManager;
    private Logger logger = LoggerFactory.getLogger(VirksomhetRepositoryImpl.class);

    public VirksomhetRepositoryImpl() {
        // CDI
    }

    @Inject
    public VirksomhetRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    @Override
    public Optional<Virksomhet> hent(String orgnr) {
        final Optional<VirksomhetEntitet> virksomhetEntitet = getVirksomhet(orgnr, false);
        if (virksomhetEntitet.isPresent()) {
            VirksomhetEntitet value = virksomhetEntitet.get();
            entityManager.detach(value);
            return Optional.of(value);
        }
        return Optional.empty();
    }

    private Optional<VirksomhetEntitet> getVirksomhet(String orgnr, boolean forWrite) {
        final TypedQuery<VirksomhetEntitet> query = entityManager.createQuery("FROM Virksomhet WHERE orgnr = :orgnr", VirksomhetEntitet.class);
        query.setParameter("orgnr", orgnr);
        if (forWrite) {
            query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        }
        return HibernateVerktøy.hentUniktResultat(query);
    }

    @Override
    public void lagre(Virksomhet virksomhet) {
        Optional<VirksomhetEntitet> virksomhet1 = getVirksomhet(virksomhet.getOrgnr(), true);
        VirksomhetEntitet.Builder builder = virksomhet1.map(VirksomhetEntitet.Builder::new)
            .orElse(new VirksomhetEntitet.Builder(virksomhet));
        builder.medRegistrert(virksomhet.getRegistrert())
            .medOppstart(virksomhet.getOppstart())
            .medNavn(virksomhet.getNavn())
            .medOrganisasjonstype(virksomhet.getOrganisasjonstype())
            .oppdatertOpplysningerNå();
        virksomhet = builder.build();
        entityManager.persist(virksomhet);
        try {
            entityManager.flush();
            entityManager.detach(virksomhet);
        } catch (PersistenceException exception) {
            if (exception.getCause() instanceof ConstraintViolationException) {
                logger.info("Prøver å lagre duplikat virksomhet={}.", virksomhet);
                throw new VirksomhetAlleredeLagretException();
            }
            throw exception;
        }
    }

}
