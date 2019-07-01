package no.nav.foreldrepenger.abakus.domene.iay;

import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;

public interface ByggInntektArbeidYtelseRepository {

    void lagre(KoblingReferanse koblingReferanse, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder);

    GrunnlagReferanse lagre(KoblingReferanse koblingReferanse, OppgittOpptjeningBuilder opptjeningBuilder);

}
