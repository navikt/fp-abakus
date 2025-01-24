package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ArbeidsavtaleRS(BigDecimal stillingsprosent, LocalDate sistLoennsendring, PeriodeRS gyldighetsperiode) {}
