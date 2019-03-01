package no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding;

import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface UtsettelsePeriode {
    /**
     * Perioden som utsettes
     *
     * @return perioden
     */
    DatoIntervallEntitet getPeriode();

    /**
     * Årsaken til utsettelsen
     *
     * @return utsettelseårsaken
     */
    UtsettelseÅrsak getÅrsak();
}
