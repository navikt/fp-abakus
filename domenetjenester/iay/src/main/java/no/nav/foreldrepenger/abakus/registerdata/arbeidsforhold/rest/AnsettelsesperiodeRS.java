package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AnsettelsesperiodeRS ( @JsonProperty("periode") PeriodeRS periode ) {
    @Override
    public String toString() {
        return "AnsettelsesperiodeRS{" + "periode=" + periode + '}';
    }
}
