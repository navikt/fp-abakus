package no.nav.foreldrepenger.abakus.app.konfig;

import static org.assertj.core.api.Fail.fail;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Request;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

class RestApiAbacTest {

    private static String PREV_LB_URL;

    /**
     * IKKE ignorer denne testen, sikrer at REST-endepunkter får tilgangskontroll
     * <p>
     * Kontakt Team Humle hvis du trenger hjelp til å endre koden din slik at den går igjennom her *
     */
    @Test
    void test_at_alle_restmetoder_er_annotert_med_BeskyttetRessurs() throws Exception {
        for (var restMethod : RestApiTester.finnAlleRestMetoder()) {
            if (restMethod.getAnnotation(BeskyttetRessurs.class) == null) {
                fail("Mangler @" + BeskyttetRessurs.class.getSimpleName() + "-annotering på " + restMethod);
            }
        }
    }

    @Test
    void sjekk_at_ingen_metoder_er_annotert_med_dummy_verdier() {
        for (var metode : RestApiTester.finnAlleRestMetoder()) {
            assertAtIngenBrukerDummyVerdierPåBeskyttetRessurs(metode);
        }
    }

    /**
     * IKKE ignorer denne testen, helper til med at input til tilgangskontroll blir riktig
     * <p>
     * Kontakt Team Humle hvis du trenger hjelp til å endre koden din slik at den går igjennom her *
     */
    @Test
    void test_at_minst_en_input_parametre_til_restmetoder_implementer_AbacDto() {
        var feilmelding = "Parameter på %s.%s av type %s må implementere " + AbacDto.class.getSimpleName() + ".\n";
        var feilmeldinger = new StringBuilder();

        for (var restMethode : RestApiTester.finnAlleRestMetoder()) {
            for (var parameter : restMethode.getParameters()) {

                if (Collection.class.isAssignableFrom(parameter.getType())) {
                    var type = (ParameterizedType) parameter.getParameterizedType();
                    @SuppressWarnings("rawtypes") Class<?> aClass = (Class) (type.getActualTypeArguments()[0]);
                    if (!AbacDto.class.isAssignableFrom(aClass) && !parameter.isAnnotationPresent(TilpassetAbacAttributt.class)
                        && !IgnorerteInputTyper.ignore(aClass)) {
                        feilmeldinger.append(String.format(feilmelding, restMethode.getDeclaringClass().getSimpleName(), restMethode.getName(),
                            aClass.getSimpleName()));
                    }
                } else {
                    if (!AbacDto.class.isAssignableFrom(parameter.getType()) && !parameter.isAnnotationPresent(TilpassetAbacAttributt.class)
                        && !IgnorerteInputTyper.ignore(parameter.getType())) {
                        feilmeldinger.append(String.format(feilmelding, restMethode.getDeclaringClass().getSimpleName(), restMethode.getName(),
                            parameter.getType().getSimpleName()));
                    }
                }
            }
        }
        if (feilmeldinger.length() > 0) {
            fail("Følgende inputparametre til REST-tjenester mangler AbacDto-impl\n" + feilmeldinger);
        }
    }

    private void assertAtIngenBrukerDummyVerdierPåBeskyttetRessurs(Method metode) {
        var klasse = metode.getDeclaringClass();
        var annotation = metode.getAnnotation(BeskyttetRessurs.class);
        if (annotation != null && annotation.actionType() == ActionType.DUMMY) {
            fail(klasse.getSimpleName() + "." + metode.getName() + " Ikke bruk DUMMY-verdi for " + ActionType.class.getSimpleName());
        } else if (annotation != null && annotation.resource().isEmpty() && annotation.property().isEmpty()
            && annotation.resourceType() == ResourceType.DUMMY) {
            fail(klasse.getSimpleName() + "." + metode.getName() + " En verdi for resource må være satt!");
        }
    }

    /**
     * Disse typene slipper naturligvis krav om impl av {@link AbacDto}
     */
    enum IgnorerteInputTyper {
        BOOLEAN(Boolean.class),
        SERVLET(HttpServletRequest.class),
        ENUM(Enum.class),
        UUID(UUID.class),
        REQUEST(Request.class),
        ;

        private Class<?> clazz;

        IgnorerteInputTyper(Class<?> clazz) {
            this.clazz = clazz;
        }

        static boolean ignore(Class<?> klasse) {
            return Arrays.stream(IgnorerteInputTyper.values()).anyMatch(e -> e.clazz.isAssignableFrom(klasse));
        }
    }
}
