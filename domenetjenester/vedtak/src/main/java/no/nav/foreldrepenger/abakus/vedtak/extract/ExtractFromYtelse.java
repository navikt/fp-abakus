package no.nav.foreldrepenger.abakus.vedtak.extract;

import java.util.Optional;

import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseBuilder;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseEntitet;
import no.nav.vedtak.ytelse.Ytelse;

public interface ExtractFromYtelse<T extends Ytelse> {
    VedtakYtelseBuilder extractFrom(T ytelse);
    Optional<VedtakYtelseEntitet> hentSisteVedtatteFor(T ytelse);
}
