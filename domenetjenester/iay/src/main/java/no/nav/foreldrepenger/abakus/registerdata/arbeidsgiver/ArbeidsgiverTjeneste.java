package no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver;


import no.nav.foreldrepenger.abakus.domene.iay.ArbeidsgiverEntitet;

public interface ArbeidsgiverTjeneste {

    ArbeidsgiverOpplysninger hent(ArbeidsgiverEntitet arbeidsgiver);
}
