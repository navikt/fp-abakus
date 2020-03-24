package no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;

public interface OppgittAnnenAktivitet {

    ArbeidType getArbeidType();

    IntervallEntitet getPeriode();
}
