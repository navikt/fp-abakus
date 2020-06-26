package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag;

import java.time.Year;
import java.util.Map;
import java.util.Optional;

public class SigrunSummertSkattegrunnlagResponse {

    Map<Year, Optional<SSGResponse>> summertskattegrunnlagMap;

    public SigrunSummertSkattegrunnlagResponse(Map<Year, Optional<SSGResponse>> summertskattegrunnlagMap) {
        this.summertskattegrunnlagMap = summertskattegrunnlagMap;
    }

    public Map<Year, Optional<SSGResponse>> getSummertskattegrunnlagMap() {
        return summertskattegrunnlagMap;
    }
}
