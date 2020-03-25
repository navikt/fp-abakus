package no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding;

import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;

public interface UtsettelsePeriode {
    /**
     * Perioden som utsettes
     *
     * @return perioden
     */
    IntervallEntitet getPeriode();

    /**
     * Årsaken til utsettelsen
     *
     * @return utsettelseårsaken
     */
    UtsettelseÅrsak getÅrsak();
}
