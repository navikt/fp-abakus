package no.nav.abakus.iaygrunnlag.arbeidsforhold.v1;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.abakus.iaygrunnlag.Periode;

import java.math.BigDecimal;


public record ArbeidsavtaleDto(@Valid @NotNull Periode periode,
                               @Valid BigDecimal stillingsprosent) {
}
