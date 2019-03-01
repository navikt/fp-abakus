package no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver;


import no.nav.foreldrepenger.abakus.domene.virksomhet.Arbeidsgiver;

public interface ArbeidsgiverTjeneste {

    ArbeidsgiverOpplysninger hent(Arbeidsgiver arbeidsgiver);
}
