package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PermisjonPermitteringRS(PeriodeRS periode, BigDecimal prosent, String type) {

}
