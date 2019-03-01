package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsKilde;

public interface InntektTjeneste {

    InntektsInformasjon finnInntekt(FinnInntektRequest finnInntektRequest, InntektsKilde inntektsKilde);
}
