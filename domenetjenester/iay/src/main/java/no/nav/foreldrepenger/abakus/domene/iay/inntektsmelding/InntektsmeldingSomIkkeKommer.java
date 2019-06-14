package no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding;

import java.util.Objects;

import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;

public class InntektsmeldingSomIkkeKommer {

    private Arbeidsgiver arbeidsgiver;
    private InternArbeidsforholdRef internRef;
    private EksternArbeidsforholdRef eksternRef;

    public InntektsmeldingSomIkkeKommer(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef internRef, EksternArbeidsforholdRef eksternRef) {
        this.arbeidsgiver = arbeidsgiver;
        this.internRef = Objects.requireNonNull(internRef, "internRef");
        this.eksternRef = eksternRef;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public InternArbeidsforholdRef getInternRef() {
        return internRef;
    }
    
    public EksternArbeidsforholdRef getEksternRef() {
        return eksternRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InntektsmeldingSomIkkeKommer that = (InntektsmeldingSomIkkeKommer) o;
        return Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
            Objects.equals(internRef, that.internRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiver, internRef);
    }

    @Override
    public String toString() {
        return "InntektsmeldingSomIkkeKommer{" +
            "arbeidsgiver=" + arbeidsgiver +
            ", ref=" + internRef +
            '}';
    }
}
