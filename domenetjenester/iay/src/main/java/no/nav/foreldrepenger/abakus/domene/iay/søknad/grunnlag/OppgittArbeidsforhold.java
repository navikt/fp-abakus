package no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag;

import java.time.LocalDate;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface OppgittArbeidsforhold {

    LocalDate getFraOgMed();

    LocalDate getTilOgMed();

    DatoIntervallEntitet getPeriode();

    Boolean erUtenlandskInntekt();

    ArbeidType getArbeidType();

    OppgittVirksomhet getUtenlandskVirksomhet();
}
