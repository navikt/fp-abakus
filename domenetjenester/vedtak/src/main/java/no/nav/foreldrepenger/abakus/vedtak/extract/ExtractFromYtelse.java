package no.nav.foreldrepenger.abakus.vedtak.extract;

import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseBuilder;
import no.nav.vedtak.ytelse.Ytelse;

public interface ExtractFromYtelse<T extends Ytelse> {
    VedtakYtelseBuilder extractFrom(T ytelse);
}
