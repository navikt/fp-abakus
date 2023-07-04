package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AnsettelsesperiodeRS {

    @JsonProperty("periode")
    private PeriodeRS periode;

    public PeriodeRS getPeriode() {
        return periode;
    }

    @Override
    public String toString() {
        return "AnsettelsesperiodeRS{" + "periode=" + periode + '}';
    }
}
