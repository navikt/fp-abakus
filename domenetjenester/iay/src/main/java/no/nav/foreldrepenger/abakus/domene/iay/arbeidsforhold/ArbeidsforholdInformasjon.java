package no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold;

import java.util.List;

import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.domene.virksomhet.Arbeidsgiver;

public interface ArbeidsforholdInformasjon {
    ArbeidsforholdRef finnForEkstern(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef ref);

    ArbeidsforholdRef finnEllerOpprett(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef ref);

    List<ArbeidsforholdOverstyringEntitet> getOverstyringer();

    ArbeidsforholdRef finnForEksternBeholdHistoriskReferanse(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef arbeidsforholdRef);
}
