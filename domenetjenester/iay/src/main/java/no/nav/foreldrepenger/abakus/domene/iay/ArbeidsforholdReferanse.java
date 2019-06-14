package no.nav.foreldrepenger.abakus.domene.iay;

import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;

public interface ArbeidsforholdReferanse {

    InternArbeidsforholdRef getInternReferanse();

    EksternArbeidsforholdRef getEksternReferanse();

    Arbeidsgiver getArbeidsgiver();

}