package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.pgifolketrygden;

import java.time.Year;
import java.util.List;
import java.util.Map;

public record SigrunPgiFolketrygdenResponse(Map<Year, List<PgiFolketrygdenResponse>> pgiFolketrygdenMap) {
}
