package no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface OppgittAnnenAktivitet {

    ArbeidType getArbeidType();

    DatoIntervallEntitet getPeriode();
}
