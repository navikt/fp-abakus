package no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag;

import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;

public interface OppgittFrilansoppdrag {

    IntervallEntitet getPeriode();

    String getOppdragsgiver();
}
