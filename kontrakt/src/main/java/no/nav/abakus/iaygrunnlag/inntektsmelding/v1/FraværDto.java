package no.nav.abakus.iaygrunnlag.inntektsmelding.v1;

import java.time.Duration;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.iaygrunnlag.Periode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.ALWAYS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class FraværDto {

    @JsonProperty(value = "periode", required = true)
    @Valid
    @NotNull
    private Periode periode;

    /** Hvis satt, angir en del av en dag. hvis ikke satt - regn hele dager med fravær. */
    @JsonProperty(value = "varighetPerDag")
    @Valid
    private Duration varighetPerDag;

    @JsonCreator
    public FraværDto(@JsonProperty(value = "periode", required = true) @NotNull Periode periode,
                     @JsonProperty(value = "varighetPerDag") Duration varighetPerDag) {
        this.periode = Objects.requireNonNull(periode, "periode");
        this.varighetPerDag = varighetPerDag;
    }

    public FraværDto(Periode periode) {
        this(periode, null);
    }

    public Periode getPeriode() {
        return periode;
    }

    public Duration getVarighetPerDag() {
        return varighetPerDag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        var other = (FraværDto) obj;
        return Objects.equals(periode, other.periode)
            && Objects.equals(varighetPerDag, other.varighetPerDag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, varighetPerDag);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
            "<periode=" + periode +
            (varighetPerDag == null ? "" : ", varighetPerDag=" + varighetPerDag) +
            ">";
    }
}
