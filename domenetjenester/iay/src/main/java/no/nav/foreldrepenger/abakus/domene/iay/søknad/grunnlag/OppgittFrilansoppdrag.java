package no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag;

import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface OppgittFrilansoppdrag {

    DatoIntervallEntitet getPeriode();

    String getOppdragsgiver();
}
