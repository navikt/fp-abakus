package no.nav.foreldrepenger.abakus.iay.impl;

import java.util.HashMap;
import java.util.Map;

import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;

/**
 * Utleder differanse i inntektsmeldinger mellom to set.
 * Tar utangspunkt i "førsteSet" og legger alle inntektsmeldinger som ikke finnes i "andreSet"
 * til i en liste og returnerer disse
 */
public final class InntektsmeldingDiffTjeneste {

    private InntektsmeldingDiffTjeneste() {
        // Skjuler default
    }

    public static Map<Inntektsmelding, ArbeidsforholdInformasjon> utledDifferanseIInntektsmeldinger(Map<Inntektsmelding, ArbeidsforholdInformasjon> førsteSet,
                                                                                                    Map<Inntektsmelding, ArbeidsforholdInformasjon> andreSet) {
        Map<Inntektsmelding, ArbeidsforholdInformasjon> diffMap = new HashMap<>();
        førsteSet.forEach((im, value) -> {
            if (!andreSet.containsKey(im)) {
                diffMap.put(im, value);
            }
        });
        return diffMap;
    }
}
