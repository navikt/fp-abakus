package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class OpplysningspliktigArbeidsgiverRS {

    @JsonProperty("type")
    private Type type;
    @JsonProperty("organisasjonsnummer")
    private String organisasjonsnummer;
    @JsonProperty("aktoerId")
    private String aktoerId;
    @JsonProperty("offentligIdent")
    private String offentligIdent;

    public Type getType() {
        return type;
    }

    public String getOrganisasjonsnummer() {
        return organisasjonsnummer;
    }

    public String getAktoerId() {
        return aktoerId;
    }

    public String getOffentligIdent() {
        return offentligIdent;
    }

    @Override
    public String toString() {
        return "OpplysningspliktigArbeidsgiverRS{" + "type=" + type + ", organisasjonsnummer='" + organisasjonsnummer + '\'' + ", aktoerId='"
            + aktoerId + '\'' + ", offentligIdent='" + offentligIdent + '\'' + '}';
    }

    public enum Type {
        Organisasjon,
        Person
    }
}
