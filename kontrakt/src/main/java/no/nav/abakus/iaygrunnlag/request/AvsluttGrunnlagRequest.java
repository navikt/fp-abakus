package no.nav.abakus.iaygrunnlag.request;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.abakus.iaygrunnlag.PersonIdent;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
public class AvsluttGrunnlagRequest {

    /**
     * Saksnummer alle grunnlag og koblinger er linket til.
     */
    @JsonProperty(value = "saksnummer", required = true)
    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9_\\.\\-]+$", message = "[${validatedValue}] matcher ikke tillatt pattern '{value}'")
    @Valid
    private String saksnummer;

    @JsonProperty(value = "referanse", required = true)
    @Valid
    @NotNull
    private UUID referanse;

    @JsonProperty(value = "ytelseType", required = true)
    @NotNull
    private YtelseType ytelseType;

    @JsonProperty(value = "aktør", required = true)
    @NotNull
    @Valid
    private PersonIdent aktør;

    @JsonCreator
    public AvsluttGrunnlagRequest(@JsonProperty(value = "saksnummer", required = true) @Valid @NotNull String saksnummer,
                                  @JsonProperty(value = "referanse", required = true) @Valid @NotNull UUID referanse,
                                  @JsonProperty(value = "ytelseType", required = true) @NotNull YtelseType ytelseType,
                                  @JsonProperty(value = "aktør", required = true) @NotNull @Valid PersonIdent aktør) {
        this.saksnummer = saksnummer;
        this.referanse = referanse;
        this.ytelseType = ytelseType;
        this.aktør = aktør;
    }

    public UUID getReferanse() {
        return referanse;
    }

    public PersonIdent getAktør() {
        return aktør;
    }

    public YtelseType getYtelseType() {
        return ytelseType;
    }

    public String getSaksnummer() {
        return saksnummer;
    }
}
