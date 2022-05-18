package no.nav.abakus.iaygrunnlag.request;

import java.util.Objects;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.iaygrunnlag.FnrPersonident;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class HentBrukersK9YtelserIPeriodeRequest {

    @JsonProperty(value = "person", required = true)
    @NotNull
    @Valid
    private FnrPersonident personident;

    @JsonProperty(value = "periode", required = true)
    @NotNull
    @Valid
    private Periode periode;

    public HentBrukersK9YtelserIPeriodeRequest() {
    }

    public HentBrukersK9YtelserIPeriodeRequest(FnrPersonident personident, Periode periode) {
        this.personident = Objects.requireNonNull(personident);
        this.periode = Objects.requireNonNull(periode);
    }

    public FnrPersonident getPersonident() {
        return personident;
    }

    public Periode getPeriode() {
        return periode;
    }
}
