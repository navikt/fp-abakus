package no.nav.foreldrepenger.abakus.iay.impl;

import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;

import java.util.HashMap;
import java.util.Map;

public final class InntektsmeldingDiffTjeneste {

    private InntektsmeldingDiffTjeneste() {
        // Skjuler default
    }

    public static Map<Inntektsmelding, ArbeidsforholdInformasjon> utledDifferanseIInntektsmeldinger(Map<Inntektsmelding, ArbeidsforholdInformasjon> førsteSet,
                                                                                                    Map<Inntektsmelding, ArbeidsforholdInformasjon> andreSet) {
        if (førsteSet.size() >= andreSet.size()) {
            return utledDiffISet(førsteSet, andreSet);
        } else {
            return utledDiffISet(andreSet, førsteSet);
        }
    }

    private static Map<Inntektsmelding, ArbeidsforholdInformasjon> utledDiffISet(Map<Inntektsmelding, ArbeidsforholdInformasjon> størsteSet,
                                                                                 Map<Inntektsmelding, ArbeidsforholdInformasjon> minsteSet) {
        Map<Inntektsmelding, ArbeidsforholdInformasjon> diffMap = new HashMap<>();
        størsteSet.forEach((im, value) -> {
            if (!minsteSet.containsKey(im)) {
                diffMap.put(im, value);
            }
        });
        return diffMap;
    }
}
