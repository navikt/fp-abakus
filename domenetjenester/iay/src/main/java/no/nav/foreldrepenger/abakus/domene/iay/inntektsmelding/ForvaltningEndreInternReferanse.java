package no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding;

import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;

public class ForvaltningEndreInternReferanse {

    public static void endreReferanse(Inntektsmelding im, InternArbeidsforholdRef internArbeidsforholdRef) {
        im.setArbeidsforholdId(internArbeidsforholdRef);
    }

}
