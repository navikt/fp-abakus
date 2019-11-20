package no.nav.foreldrepenger.abakus.domene.virksomhet;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.vedtak.util.LRUCache;

@ApplicationScoped
public class VirksomhetRepositoryImpl implements VirksomhetRepository {

    private LRUCache<String, Virksomhet> cache;

    public VirksomhetRepositoryImpl() {
        this.cache = new LRUCache<>(1000, 60 * 1000 * 15);
    }

    @Override
    public Optional<Virksomhet> hent(String orgnr) {
        return Optional.ofNullable(cache.get(orgnr));
    }

    @Override
    public void lagre(Virksomhet virksomhet) {
        Objects.requireNonNull(virksomhet, "virksomhet kan ikke være null");
        cache.put(virksomhet.getOrgnr(), virksomhet);
    }

}
