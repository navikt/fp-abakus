package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

public interface InntektArbeidYtelseAggregat {

    Collection<AktørInntekt> getAktørInntekt();

    Collection<AktørArbeid> getAktørArbeid();

    Collection<AktørYtelse> getAktørYtelse();

    LocalDateTime getOpprettetTidspunkt();

    /** Identifisere en immutable instans av grunnlaget unikt og er egnet for utveksling (eks. til abakus eller andre systemer) */
    UUID getEksternReferanse();
}
