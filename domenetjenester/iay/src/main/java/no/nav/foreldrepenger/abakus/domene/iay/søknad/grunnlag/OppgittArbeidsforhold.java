package no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag;

import java.time.LocalDate;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.domene.virksomhet.Virksomhet;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface OppgittArbeidsforhold {

    OrgNummer getOrgnummer();

    LocalDate getFraOgMed();

    LocalDate getTilOgMed();

    DatoIntervallEntitet getPeriode();

    Boolean erUtenlandskInntekt();

    ArbeidType getArbeidType();

    UtenlandskVirksomhet getUtenlandskVirksomhet();
}
