package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.List;

import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;

public interface InntektsmeldingAggregat {

    /**
     * Alle gjeldende inntektsmeldinger i behandlingen.
     *
     * @return Liste med {@link Inntektsmelding}
     */
    List<Inntektsmelding> getInntektsmeldinger();

    /**
     * Alle gjeldende inntektsmeldinger for en virksomhet i behandlingen.
     *
     * @return Liste med {@link Inntektsmelding}
     */
    List<Inntektsmelding> getInntektsmeldingerFor(Arbeidsgiver arbeidsgiver);
    
    /** Get alle inntetksmeldinger (både de som skal brukes og ikke brukes). */
    List<Inntektsmelding> getAlleInntektsmeldinger();

    Long getId();
}
