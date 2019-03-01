package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Collection;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.abakus.domene.virksomhet.Arbeidsgiver;

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
     * @return {@link Arbeidsgiver}
     */
    Arbeidsgiver getArbeidsgiver();

    /**
     * Utbetalinger utført av utbetaler
     *
     * @return liste av {@link Inntektspost}
     */
    Collection<Inntektspost> getInntektspost();

}
