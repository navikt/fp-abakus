package no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.rs;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MeldekortUtbetalingsgrunnlagMeldekortDto(BigDecimal beløp,
                                                       BigDecimal dagsats,
                                                       LocalDate meldekortFom,
                                                       LocalDate meldekortTom,

                                                       BigDecimal utbetalingsgrad) {

}
