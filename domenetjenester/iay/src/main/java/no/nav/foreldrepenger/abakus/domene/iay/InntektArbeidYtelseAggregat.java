package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Collection;

public interface InntektArbeidYtelseAggregat {

    Collection<AktørInntekt> getAktørInntekt();

    Collection<AktørArbeid> getAktørArbeid();

    Collection<AktørYtelse> getAktørYtelse();

}
