package no.nav.abakus.iaygrunnlag.request;

import java.util.Set;
import java.util.UUID;

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
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.PersonIdent;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
public class InnhentRegisterdataRequest {

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

    @JsonProperty(value = "opplysningsperiode", required = true)
    @NotNull
    @Valid
    private Periode opplysningsperiode;

    @JsonProperty(value = "opplysningsperiodeSkattegrunnlag")
    @Valid
    private Periode opplysningsperiodeSkattegrunnlag;

    @JsonProperty(value = "aktør", required = true)
    @NotNull
    @Valid
    private PersonIdent aktør;

    @JsonProperty(value = "annenPartAktør")
    @Valid
    private PersonIdent annenPartAktør;

    @JsonProperty(value = "opptjeningsperiode")
    @Valid
    private Periode opptjeningsperiode;

    @JsonProperty(value = "elementer")
    @Valid
    private Set<RegisterdataType> elementer = Set.of(RegisterdataType.ARBEIDSFORHOLD, RegisterdataType.INNTEKT_PENSJONSGIVENDE,
        RegisterdataType.YTELSE);

    public InnhentRegisterdataRequest(@JsonProperty(value = "saksnummer", required = true) @Valid @NotNull String saksnummer,
                                      @JsonProperty(value = "referanse", required = true) @Valid @NotNull UUID referanse,
                                      @JsonProperty(value = "ytelseType", required = true) @NotNull YtelseType ytelseType,
                                      @JsonProperty(value = "opplysningsperiode", required = true) @NotNull @Valid Periode opplysningsperiode,
                                      @JsonProperty(value = "aktør", required = true) @NotNull @Valid PersonIdent aktør) {
        this.saksnummer = saksnummer;
        this.referanse = referanse;
        this.ytelseType = ytelseType;
        this.opplysningsperiode = opplysningsperiode;
        this.aktør = aktør;
    }

    @JsonCreator
    public InnhentRegisterdataRequest(@JsonProperty(value = "saksnummer", required = true) @Valid @NotNull String saksnummer,
                                      @JsonProperty(value = "referanse", required = true) @Valid @NotNull UUID referanse,
                                      @JsonProperty(value = "ytelseType", required = true) @NotNull YtelseType ytelseType,
                                      @JsonProperty(value = "opplysningsperiode", required = true) @NotNull @Valid Periode opplysningsperiode,
                                      @JsonProperty(value = "aktør", required = true) @NotNull @Valid PersonIdent aktør,
                                      @JsonProperty(value = "elementer", required = true) @NotNull @Valid Set<RegisterdataType> elementer) {
        this(saksnummer, referanse, ytelseType, opplysningsperiode, aktør);
        this.elementer = elementer;
    }

    public UUID getReferanse() {
        return referanse;
    }

    public PersonIdent getAktør() {
        return aktør;
    }

    public PersonIdent getAnnenPartAktør() {
        return annenPartAktør;
    }

    public void setAnnenPartAktør(PersonIdent annenPartAktør) {
        this.annenPartAktør = annenPartAktør;
    }

    public Periode getOpplysningsperiode() {
        return opplysningsperiode;
    }

    public Periode getOpplysningsperiodeSkattegrunnlag() {
        return opplysningsperiodeSkattegrunnlag;
    }

    public void setOpplysningsperiodeSkattegrunnlag(Periode opplysningsperiodeSkattegrunnlag) {
        this.opplysningsperiodeSkattegrunnlag = opplysningsperiodeSkattegrunnlag;
    }

    public Periode getOpptjeningsperiode() {
        return opptjeningsperiode;
    }

    public void setOpptjeningsperiode(Periode opptjeningsperiode) {
        this.opptjeningsperiode = opptjeningsperiode;
    }

    public Set<RegisterdataType> getElementer() {
        return elementer;
    }

    public void setElementer(Set<RegisterdataType> elementer) {
        this.elementer = elementer;
    }

    public YtelseType getYtelseType() {
        return ytelseType;
    }

    public String getSaksnummer() {
        return saksnummer;
    }
}
