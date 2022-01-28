package no.nav.abakus.iaygrunnlag.request;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.abakus.iaygrunnlag.FnrPersonident;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class HentBrukersYtelserIPeriodeRequest {

    @JsonProperty(value = "person", required = true)
    @NotNull
    @Valid
    private FnrPersonident personident;

    @JsonProperty(value = "periode", required = true)
    @NotNull
    @Valid
    private Periode periode;

    @JsonProperty(value = "ytelser")
    private Set<YtelseType> ytelser = Set.of(YtelseType.PLEIEPENGER_NÆRSTÅENDE,
        YtelseType.FORELDREPENGER,
        YtelseType.OMSORGSPENGER,
        YtelseType.OPPLÆRINGSPENGER,
        YtelseType.FRISINN,
        YtelseType.SVANGERSKAPSPENGER,
        YtelseType.PLEIEPENGER_SYKT_BARN);

    public HentBrukersYtelserIPeriodeRequest() {
    }

    public HentBrukersYtelserIPeriodeRequest(FnrPersonident personident, Periode periode, Set<YtelseType> ytelser) {
        this.personident = Objects.requireNonNull(personident);
        this.periode = Objects.requireNonNull(periode);
        this.ytelser = Objects.requireNonNull(ytelser);
    }

    public FnrPersonident getPersonident() {
        return personident;
    }

    public Periode getPeriode() {
        return periode;
    }

    public Set<YtelseType> getYtelser() {
        return ytelser;
    }
}
