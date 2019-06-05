package no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;

public interface ArbeidsforholdInformasjon {

    @Deprecated(forRemoval=true)
    ArbeidsforholdRef finnEllerOpprett(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef ref);


    @Deprecated(forRemoval=true)
    ArbeidsforholdRef finnForEksternBeholdHistoriskReferanse(Arbeidsgiver arbeidsgiver, ArbeidsforholdRef arbeidsforholdRef);
    
    List<ArbeidsforholdOverstyringEntitet> getOverstyringer();
    
    Optional<InternArbeidsforholdRef> finnForEkstern(Arbeidsgiver arbeidsgiver, EksternArbeidsforholdRef ref);

    InternArbeidsforholdRef finnEllerOpprett(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef ref);
    InternArbeidsforholdRef finnEllerOpprett(Arbeidsgiver arbeidsgiver, EksternArbeidsforholdRef ref);
    
    Optional<InternArbeidsforholdRef> finnForEksternBeholdHistoriskReferanse(Arbeidsgiver arbeidsgiver, EksternArbeidsforholdRef arbeidsforholdRef);

    EksternArbeidsforholdRef finnEkstern(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef internReferanse);
    
    Collection<ArbeidsforholdReferanseEntitet> getArbeidsforholdReferanser();

}
