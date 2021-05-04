package no.nav.foreldrepenger.abakus.felles.sikkerhet;

/**
 * Brukes for å mappe om resource-felter for REST-tjenester som oppgir resource som ikke er i duplo-sikkerhetsdomene
 */
public class AbacResourceMapper {

    private AbacResourceMapper() {}

    public static String mapResource(String resource) {
        if (AbakusBeskyttetRessursAttributt.ALLE_GODKJENTE_KODER.contains(resource)) {
            return resource;
        }
        throw new IllegalArgumentException("Utvikler-feil: Ukjent ABAC resource: " + resource);
    }
}
