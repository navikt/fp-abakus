package no.nav.foreldrepenger.abakus.kodeverk;

import static no.nav.foreldrepenger.abakus.kodeverk.KodeverkFeil.FEILFACTORY;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.jpa.QueryHints;

import no.nav.vedtak.util.LRUCache;

@ApplicationScoped
public class KodeverkRepository {

    private static final long CACHE_ELEMENT_LIVE_TIME = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
    LRUCache<String, Kodeliste> kodelisteCache = new LRUCache<>(1000, CACHE_ELEMENT_LIVE_TIME);
    private EntityManager entityManager;

    KodeverkRepository() {
        // for CDI proxy
    }

    @Inject
    public KodeverkRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    public <V extends Kodeliste> V finn(Class<V> cls, String kode) {
        String cacheKey = cls.getName() + kode;
        @SuppressWarnings("unchecked")
        Optional<V> fraCache = Optional.ofNullable((V) kodelisteCache.get(cacheKey));
        return fraCache.orElseGet(() -> {
            V finnEM = finnFraEM(cls, kode);
            kodelisteCache.put(cacheKey, finnEM);
            return finnEM;
        });
    }

    public <V extends Kodeliste> V finn(Class<V> cls, V kodelisteKonstant) {
        return finn(cls, kodelisteKonstant.getKode());
    }

    public <V extends Kodeliste> V finnForKodeverkEiersKode(Class<V> cls, String offisiellKode) {
        String cacheKey = cls.getName() + offisiellKode;
        @SuppressWarnings("unchecked")
        Optional<V> fraCache = Optional.ofNullable((V) kodelisteCache.get(cacheKey));
        return fraCache.orElseGet(() -> {
            V result = finnForKodeverkEiersKodeFraEM(cls, offisiellKode);
            kodelisteCache.put(cacheKey, result);
            return result;
        });
    }

    private <V extends Kodeliste> V finnFraEM(Class<V> cls, String kode) {
        CriteriaQuery<V> criteria = createCriteria(cls, Collections.singletonList(kode));
        try {
            return entityManager.createQuery(criteria)
                .setHint(QueryHints.HINT_READONLY, "true")
                .getSingleResult();
        } catch (NoResultException e) {
            throw FEILFACTORY.kanIkkeFinneKodeverk(cls.getSimpleName(), kode, e).toException();
        }
    }

    private <V extends Kodeliste> V finnForKodeverkEiersKodeFraEM(Class<V> cls, String offisiellKode) {
        CriteriaQuery<V> criteria = createCriteria(cls, "offisiellKode", Collections.singletonList(offisiellKode));
        try {
            return entityManager.createQuery(criteria)
                .setHint(QueryHints.HINT_READONLY, "true")
                .getSingleResult();
        } catch (NoResultException e) {
            throw FEILFACTORY.kanIkkeFinneKodeverkForOffisiellKode(cls.getSimpleName(), offisiellKode, e).toException();
        }
    }

    protected <V extends Kodeliste> CriteriaQuery<V> createCriteria(Class<V> cls, List<String> koder) {
        return createCriteria(cls, "kode", koder);
    }

    protected <V extends Kodeliste> CriteriaQuery<V> createCriteria(Class<V> cls, String felt, List<String> koder) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<V> criteria = builder.createQuery(cls);

        DiscriminatorValue discVal = cls.getDeclaredAnnotation(DiscriminatorValue.class);
        Objects.requireNonNull(discVal, "Mangler @DiscriminatorValue i klasse:" + cls); //$NON-NLS-1$
        String kodeverk = discVal.value();
        Root<V> from = criteria.from(cls);
        criteria.where(builder.and(
            builder.equal(from.get("kodeverk"), kodeverk), //$NON-NLS-1$
            from.get(felt).in(koder))); // $NON-NLS-1$
        return criteria;
    }
}
