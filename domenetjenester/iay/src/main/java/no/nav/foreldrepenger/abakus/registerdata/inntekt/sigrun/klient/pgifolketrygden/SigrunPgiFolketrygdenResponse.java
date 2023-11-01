package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.pgifolketrygden;

import java.time.Year;
import java.util.Map;
import java.util.Optional;

public record SigrunPgiFolketrygdenResponse(Map<Year, Optional<PgiFolketrygdenResponse>> pgiFolketrygdenMap) {
}
