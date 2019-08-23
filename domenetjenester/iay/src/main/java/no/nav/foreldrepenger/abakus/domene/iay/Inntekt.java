package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Collection;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsKilde;

public interface Inntekt {

    /**
     * System (+ filter) som inntektene er hentet inn fra / med
     *
     * @return {@link InntektsKilde}
     */
    InntektsKilde getInntektsKilde();

    /**
     * Utbetaler
     *
     * @return {@link ArbeidsgiverEntitet}
     */
    Arbeidsgiver getArbeidsgiver();

    /** Hent alle utbetalinger (ufiltrert). */
    Collection<Inntektspost> getAlleInntektsposter();
}
