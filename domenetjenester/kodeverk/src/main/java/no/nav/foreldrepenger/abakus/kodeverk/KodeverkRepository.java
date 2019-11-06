package no.nav.foreldrepenger.abakus.kodeverk;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Få tilgang til kodeverk.
 */
public interface KodeverkRepository {

    /**
     * Finn instans av Kodeliste innslag for angitt kode verdi.
     */
    <V extends Kodeliste> V finn(Class<V> cls, String kode);

    /**
     * Finn instans av Kodeliste innslag for angitt Kodeliste.
     * For oppslag av fulle instanser fra de ufullstendige i hver konkrete subklasse av Kodeliste.
     */
    <V extends Kodeliste> V finn(Class<V> cls, V kodelisteKonstant);

    /**
     * Finn instans av Kodeliste innslag for angitt offisiell kode verdi.
     */
    <V extends Kodeliste> V finnForKodeverkEiersKode(Class<V> cls, String offisiellKode);

    /**
     * Finn instans av Kodeliste innslag for angitt offisiell kode verdi, eller en default value hvis offisiell kode ikke git treff.
     */
    <V extends Kodeliste> V finnForKodeverkEiersKode(Class<V> cls, String offisiellKode, V defaultValue);

    /**
     * Finn kode, return er optional empty hvis ikke finnes.
     */
    <V extends Kodeliste> Optional<V> finnOptional(Class<V> cls, String kode);

    Map<String, Set<String>> hentAlle();
}
