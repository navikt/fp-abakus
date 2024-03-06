package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public record OpplysningspliktigArbeidsgiverRS(Type type, String organisasjonsnummer, String aktoerId, String offentligIdent) {

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
