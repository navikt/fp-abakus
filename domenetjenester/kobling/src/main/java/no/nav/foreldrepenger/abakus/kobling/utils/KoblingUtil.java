package no.nav.foreldrepenger.abakus.kobling.utils;

import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.vedtak.exception.TekniskException;

public class KoblingUtil {

    private KoblingUtil() {
        // static utility
    }

    public static void validerIkkeAvsluttet(Kobling kobling) {
        if (!kobling.erAktiv()) {
            throw new TekniskException("FT-49000", String.format(
                "Ikke tillatt å gjøre endringer på en avsluttet kobling. Gjelder kobling med referanse %s",
                kobling.getKoblingReferanse()));
        }
    }
}
