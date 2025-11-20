package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Permisjon(LocalDate permisjonFom, LocalDate permisjonTom, BigDecimal permisjonsprosent, String permisjonsÅrsak) {
}
