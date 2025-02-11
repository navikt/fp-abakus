package no.nav.abakus.iaygrunnlag.arbeidsforhold.v1;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.abakus.iaygrunnlag.Aktør;
import no.nav.abakus.iaygrunnlag.ArbeidsforholdRefDto;

import java.util.Objects;

/**
 * Et arbeidsforhold for en arbeidsgiver.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class ArbeidsforholdReferanseDto {

    @JsonProperty(value = "arbeidsgiver", required = true)
    @Valid
    @NotNull
    private Aktør arbeidsgiver;

    @JsonProperty(value = "arbeidsforholdId", required = true)
    @Valid
    @NotNull
    private ArbeidsforholdRefDto arbeidsforholdId;

    @JsonCreator
    public ArbeidsforholdReferanseDto(@JsonProperty(value = "arbeidsgiver", required = true) @Valid @NotNull Aktør arbeidsgiver,
                                      @JsonProperty(value = "arbeidsforholdId", required = true) @Valid @NotNull ArbeidsforholdRefDto arbeidsforholdId) {
        Objects.requireNonNull(arbeidsgiver, "arbeidsgiver");
        Objects.requireNonNull(arbeidsforholdId, "arbeidsforholdId");
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdId = arbeidsforholdId;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public ArbeidsforholdRefDto getArbeidsforholdReferanse() {
        return arbeidsforholdId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var other = getClass().cast(obj);
        return Objects.equals(this.arbeidsgiver, other.arbeidsgiver) && Objects.equals(this.arbeidsforholdId, other.arbeidsforholdId);

    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiver, arbeidsforholdId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<arbeidsgiver =" + arbeidsgiver + ", arbeidsforholdId=" + arbeidsforholdId + ">";
    }
}
