package no.nav.abakus.iaygrunnlag.kodeverk;

/** Kodeverk som er portet til java. */
public interface Kodeverdi extends IndexKey {

    String getKode();

    @Override
    default String getIndexKey() {
        return getKode();
    }

    default String getOffisiellKode() {
        return getKode();
    }
}
