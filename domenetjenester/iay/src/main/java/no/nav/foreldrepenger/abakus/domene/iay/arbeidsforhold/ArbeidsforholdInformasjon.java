package no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.abakus.domene.iay.ArbeidsforholdReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;

public interface ArbeidsforholdInformasjon {

    List<ArbeidsforholdOverstyringEntitet> getOverstyringer();
    
    Optional<InternArbeidsforholdRef> finnForEkstern(Arbeidsgiver arbeidsgiver, EksternArbeidsforholdRef ref);

    InternArbeidsforholdRef finnEllerOpprett(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef ref);
    InternArbeidsforholdRef finnEllerOpprett(Arbeidsgiver arbeidsgiver, EksternArbeidsforholdRef ref);
    
    Optional<InternArbeidsforholdRef> finnForEksternBeholdHistoriskReferanse(Arbeidsgiver arbeidsgiver, EksternArbeidsforholdRef arbeidsforholdRef);

    EksternArbeidsforholdRef finnEkstern(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef internReferanse);
    
    /** @deprecated Bruk {@link ArbeidsforholdInformasjonBuilder} i stedet. */
    @Deprecated(forRemoval = true)
    ArbeidsforholdReferanse opprettNyReferanse(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef internReferanse,
                                                      EksternArbeidsforholdRef eksternReferanse);
    
    Collection<ArbeidsforholdReferanseEntitet> getArbeidsforholdReferanser();

}
