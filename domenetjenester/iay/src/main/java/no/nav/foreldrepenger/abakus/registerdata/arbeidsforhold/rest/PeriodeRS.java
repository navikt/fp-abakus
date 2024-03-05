package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.mapping.List;

public record PeriodeRS (@JsonProperty("fom") LocalDate fom, @JsonProperty("tom") LocalDate tom) {
    @Override
    public String toString() {
        return "PeriodeRS{" + "fom=" + fom + ", tom=" + tom + '}';
    }
}
