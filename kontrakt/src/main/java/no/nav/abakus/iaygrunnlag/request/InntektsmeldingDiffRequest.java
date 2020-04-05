package no.nav.abakus.iaygrunnlag.request;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.abakus.iaygrunnlag.PersonIdent;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;
import java.util.UUID;

/**
 * Spesifikasjon for å hvilke
 * Må minimum angi personident og en eller flere referanser (grunnlag, kobling, saksnummer)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class InntektsmeldingDiffRequest {

    /** Angi hvem grunnlaget hentes for. */
    @JsonProperty(value = "personIdent", required = true)
    @Valid
    @NotNull
    private PersonIdent person;

    @JsonProperty(value = "ytelseType")
    @Valid
    private YtelseType ytelseType;

    /** Angi hvilken sak det gjelder. */
    @JsonProperty(value = "saksnummer")
    @Valid
    @Pattern(regexp = "^[A-Za-z0-9_\\.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'")
    private String saksnummer;

    /** Referanse én som skal brukes til sammenligning. */
    @JsonProperty(value = "eksternRefEn")
    @Valid
    @NotNull
    private UUID eksternRefEn;

    /** Referanse to som skal brukes til sammenligning. */
    @JsonProperty(value = "eksternRefTo")
    @Valid
    @NotNull
    private UUID eksternRefTo;


    protected InntektsmeldingDiffRequest() {
        // default ctor.
    }

    @JsonCreator
    public InntektsmeldingDiffRequest(@JsonProperty(value = "personIdent", required = true) @Valid @NotNull PersonIdent person) {
        this.person = Objects.requireNonNull(person, "person");
    }

    public PersonIdent getPerson() {
        return person;
    }

    public YtelseType getYtelseType() {
        return ytelseType;
    }

    public UUID getEksternRefEn() { return eksternRefEn; }

    public void setYtelseType(YtelseType ytelseType) {
        this.ytelseType = ytelseType;
    }

    public void setEksternRefEn(UUID eksternRefEn) {
        this.eksternRefEn = eksternRefEn;
    }

    public UUID getEksternRefTo() {
        return eksternRefTo;
    }

    public void setEksternRefTo(UUID eksternRefTo) {
        this.eksternRefTo = eksternRefTo;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }
}
