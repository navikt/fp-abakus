package no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag;

import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface Frilansoppdrag {

    DatoIntervallEntitet getPeriode();

    String getOppdragsgiver();
}
