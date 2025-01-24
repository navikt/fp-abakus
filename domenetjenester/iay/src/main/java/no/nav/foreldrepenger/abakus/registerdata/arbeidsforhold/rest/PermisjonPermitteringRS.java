package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest;

import java.math.BigDecimal;

public record PermisjonPermitteringRS(PeriodeRS periode, BigDecimal prosent, String type) {}
