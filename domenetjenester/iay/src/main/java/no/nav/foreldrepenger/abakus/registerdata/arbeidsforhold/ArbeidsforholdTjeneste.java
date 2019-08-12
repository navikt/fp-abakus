package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold;

import java.util.List;
import java.util.Map;

import org.threeten.extra.Interval;

import no.nav.foreldrepenger.abakus.typer.PersonIdent;

public interface ArbeidsforholdTjeneste {

    Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> finnArbeidsforholdForIdentIPerioden(PersonIdent fnr, Interval interval);

}
