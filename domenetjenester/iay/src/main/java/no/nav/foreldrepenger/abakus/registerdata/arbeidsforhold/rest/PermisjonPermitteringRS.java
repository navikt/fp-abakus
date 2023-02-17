package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PermisjonPermitteringRS {

    @JsonProperty("periode")
    private PeriodeRS periode;
    @JsonProperty("prosent")
    private BigDecimal prosent;
    @JsonProperty("type")
    private String type; // kodeverk: PermisjonsOgPermitteringsBeskrivelse

    public PeriodeRS getPeriode() {
        return periode;
    }

    public BigDecimal getProsent() {
        return prosent;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "PermisjonPermitteringRS{" + "periode=" + periode + ", prosent=" + prosent + ", type='" + type + '\'' + '}';
    }
}
