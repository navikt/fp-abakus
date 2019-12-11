package no.nav.foreldrepenger.abakus.vedtak.extract;

import no.nav.abakus.vedtak.ytelse.Ytelse;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseBuilder;

public interface ExtractFromYtelse<T extends Ytelse> {
    VedtakYtelseBuilder extractFrom(T ytelse);
}
