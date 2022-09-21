package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag;

import java.time.Year;
import java.util.Map;
import java.util.Optional;

public record SigrunSummertSkattegrunnlagResponse(Map<Year, Optional<SSGResponse>> summertskattegrunnlagMap) {
}
