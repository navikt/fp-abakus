package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PermisjonPermitteringRS (@JsonProperty("periode") PeriodeRS periode, @JsonProperty("prosent") BigDecimal prosent, @JsonProperty("type") String type) {
    @Override
    public String toString() {
        return "PermisjonPermitteringRS{" + "periode=" + periode + ", prosent=" + prosent + ", type='" + type + '\'' + '}';
    }
}
