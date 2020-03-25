package no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag;

import java.time.LocalDate;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.kodeverk.Landkoder;

public interface OppgittArbeidsforhold {

    LocalDate getFraOgMed();

    LocalDate getTilOgMed();

    IntervallEntitet getPeriode();

    Boolean erUtenlandskInntekt();

    ArbeidType getArbeidType();

    Landkoder getLandkode();

    String getUtenlandskVirksomhetNavn();
}
