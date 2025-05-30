package no.nav.abakus.iaygrunnlag.request;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.abakus.iaygrunnlag.PersonIdent;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;

/**
 * Spesifikasjon for å hente opp et InntektArbeidYtelseGrunnlag.
 * Merk at props her kan ekskludere/kombineres.
 * Må minimum angi personident og en eller flere referanser (grunnlag, kobling, saksnummer)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class InntektsmeldingerRequest {

    /**
     * Angi hvem grunnlaget hentes for.
     */
    @JsonProperty(value = "personIdent", required = true)
    @Valid
    @NotNull
    private PersonIdent person;

    @JsonProperty(value = "ytelseType")
    @Valid
    @NotNull
    private YtelseType ytelseType;

    /**
     * Angi hvilken sak det gjelder.
     */
    @JsonProperty(value = "saksnummer")
    @NotNull
    @Valid
    @Pattern(regexp = "^[A-Za-z0-9_\\.\\-:]+$", message = "[${validatedValue}] matcher ikke tillatt pattern '{value}'")
    private String saksnummer;

    protected InntektsmeldingerRequest() {
        // default ctor.
    }

    @JsonCreator
    public InntektsmeldingerRequest(@JsonProperty(value = "personIdent", required = true) @Valid @NotNull PersonIdent person,
                                    @JsonProperty(value = "saksnummer") @Valid @NotNull String saksnummer,
                                    @JsonProperty(value = "ytelseType") @Valid @NotNull YtelseType ytelseType) {
        this.person = Objects.requireNonNull(person, "person");
        this.saksnummer = Objects.requireNonNull(saksnummer, "saksnummer");
        this.ytelseType = Objects.requireNonNull(ytelseType, "ytelseType");
    }

    public PersonIdent getPerson() {
        return person;
    }

    public YtelseType getYtelseType() {
        return ytelseType;
    }

    public String getSaksnummer() {
        return saksnummer;
    }
}
