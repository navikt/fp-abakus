package no.nav.abakus.iaygrunnlag.request;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Objects;
import no.nav.abakus.iaygrunnlag.PersonIdent;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;

/**
 * Spesifikasjon for å hente opp et InntektArbeidYtelseGrunnlag. Merk at props her kan ekskludere/kombineres. Må minimum
 * angi personident og en eller flere referanser (grunnlag, kobling, saksnummer)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(
        fieldVisibility = Visibility.NONE,
        getterVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE,
        isGetterVisibility = Visibility.NONE,
        creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class InntektsmeldingerRequest {

    /** Angi hvem grunnlaget hentes for. */
    @JsonProperty(value = "personIdent", required = true)
    @Valid
    @NotNull
    private PersonIdent person;

    @JsonProperty(value = "ytelseType")
    private YtelseType ytelseType;

    /** Angi hvilken sak det gjelder. */
    @JsonProperty(value = "saksnummer")
    @Valid
    @Pattern(regexp = "^[A-Za-z0-9_\\.\\-:]+$", message = "[${validatedValue}] matcher ikke tillatt pattern '{value}'")
    private String saksnummer;

    protected InntektsmeldingerRequest() {
        // default ctor.
    }

    @JsonCreator
    public InntektsmeldingerRequest(
            @JsonProperty(value = "personIdent", required = true) @Valid @NotNull PersonIdent person) {
        this.person = Objects.requireNonNull(person, "person");
    }

    public PersonIdent getPerson() {
        return person;
    }

    public YtelseType getYtelseType() {
        return ytelseType;
    }

    public void setYtelseType(YtelseType ytelseType) {
        this.ytelseType = ytelseType;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }
}
