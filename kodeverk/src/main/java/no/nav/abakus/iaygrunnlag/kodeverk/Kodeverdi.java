package no.nav.abakus.iaygrunnlag.kodeverk;

/** Kodeverk som er portet til java. */
public interface Kodeverdi extends IndexKey {

    String getKode();

    String getKodeverk();

    String getNavn();

    @Override
    default String getIndexKey() {
        return getKode();
    }
    
    default String getOffisiellKode() {
        return getKode();
    }

}