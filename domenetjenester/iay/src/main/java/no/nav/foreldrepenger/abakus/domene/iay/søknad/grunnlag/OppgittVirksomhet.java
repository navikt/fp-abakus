package no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag;

import no.nav.foreldrepenger.abakus.kodeverk.Landkoder;

/** Oppgitt virksomhet (uten eks. orgnr). */
public interface OppgittVirksomhet {

    Landkoder getLandkode();

    String getNavn();
}
