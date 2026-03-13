package no.nav.foreldrepenger.abakus.app.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import no.nav.abakus.iaygrunnlag.kodeverk.Kodeverdi;
import no.nav.abakus.iaygrunnlag.kodeverk.Landkode;
import no.nav.foreldrepenger.abakus.app.IndexClasses;
import no.nav.foreldrepenger.abakus.kobling.Kobling;


class KodeverdiKonsistensTest {

    // Mulig videre arbeid 1: Lag et grensesnitt "FrontendMedNavn" som implementeres av enums listet i HentKodeverdierTjeneste
    // Deretter kan man bruke Jandex til å finne alle implementasjoner i stedet for å liste eksplisitte klasser.

    // Mulig videre arbeid 2: Supplier av en map for databasekoder - så man slipper lokale "fraKode".

    @Test
    void sjekk_alle_kodeverk_kodeverdier() throws URISyntaxException {

        // Det ligger for tiden implementasjoner av Kodeverdi i 3 ulike moduler, tar inn en fra hver modul for å finne alle kodeverdi-klasser
        var indexClasses = IndexClasses.getIndexFor(Kobling.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        var kodeverdiKlasser = indexClasses.getClasses(
                ci -> true,
                c -> !c.isInterface() && !c.isAnonymousClass() && Kodeverdi.class.isAssignableFrom(c)).stream()
            .map(c -> (Class<? extends Kodeverdi>) c)
            .filter(c -> c != Landkode.class) // Landkode er String-stil-kode
            .collect(Collectors.toSet());
        // Vi ønsker kun enum-implementasjoner av Kodeverdi (utenom den ene anonyme i Årsak)
        assertThat(kodeverdiKlasser).allMatch(Class::isEnum);
        // Sjekk for duplikate koder innen hver enum
        kodeverdiKlasser.forEach(k -> {
            var antallEnum = k.getEnumConstants().length;
            var antallUnikKode = Arrays.stream(k.getEnumConstants()).map(Kodeverdi::getKode).distinct().count();
            assertThat(antallEnum).withFailMessage("Duplikate koder i %s", k.getSimpleName()).isEqualTo(antallUnikKode);
        });
        assertDoesNotThrow(() -> kodeverdiKlasser.stream()
            .map(k -> Map.entry(k.getSimpleName(),
                Arrays.stream(k.getEnumConstants()).collect(Collectors.toMap(Kodeverdi::getKode, Function.identity()))))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @Test
    void sjekk_alle_kode_kolonner_er_annotert_og_databasekode() throws URISyntaxException {
        var indexClasses = IndexClasses.getIndexFor(Kobling.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        @SuppressWarnings("unchecked")
        var entitetsklasser = indexClasses.getClasses(ci -> true, c -> c.isAnnotationPresent(Entity.class));
        var enumFields = entitetsklasser.stream()
            .flatMap(k -> Arrays.stream(k.getDeclaredFields()))
            .filter(f -> f.getType().isEnum())
            .toList();
        // Test om enum-attributt er annotert med @Enumerated eller @Convert
        var ikkeAnnotert = enumFields.stream()
            .filter(f -> !f.isAnnotationPresent(Enumerated.class) && !f.isAnnotationPresent(Convert.class))
            .toList();
        assertThat(ikkeAnnotert).isEmpty();
        // Test om enum-attributt er instans av DatabaseKode
        var ikkeDatabaseKode = enumFields.stream()
            .filter(f -> !Kodeverdi.class.isAssignableFrom(f.getType()))
            .toList();
        assertThat(ikkeDatabaseKode).isEmpty();

    }

}
