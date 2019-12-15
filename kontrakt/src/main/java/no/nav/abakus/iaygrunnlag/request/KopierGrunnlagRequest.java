package no.nav.abakus.iaygrunnlag.request;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.PersonIdent;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
public class KopierGrunnlagRequest {

    /**
     * Saksnummer alle grunnlag og koblinger er linket til.
     */
    @JsonProperty(value = "saksnummer", required = true)
    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9_\\.\\-:]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{value}'")
    @Valid
    private String saksnummer;

    @JsonProperty(value = "nyReferanse", required = true)
    @Valid
    @NotNull
    private UUID nyReferanse;

    @JsonProperty(value = "ytelseType", required = true)
    @Valid
    @NotNull
    private YtelseType ytelseType;

    @JsonProperty(value = "opplysningsperiode")
    @Valid
    private Periode opplysningsperiode;

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

    @JsonProperty(value = "gammelReferanse", required = true)
    @Valid
    @NotNull
    private UUID gammelReferanse;

    @JsonProperty("dataset")
    @Valid
    public Set<Dataset> dataset = EnumSet.of(Dataset.OPPGITT_OPPTJENING, Dataset.REGISTER, Dataset.OVERSTYRT, Dataset.INNTEKTSMELDING);

    @JsonCreator
    public KopierGrunnlagRequest(@JsonProperty(value = "saksnummer", required = true) @Valid @NotNull String saksnummer,
                                 @JsonProperty(value = "nyReferanse", required = true) @Valid @NotNull UUID nyReferanse,
                                 @JsonProperty(value = "gammelReferanse", required = true) @Valid @NotNull UUID gammelReferanse,
                                 @JsonProperty(value = "ytelseType", required = true) @Valid @NotNull YtelseType ytelseType,
                                 @JsonProperty(value = "aktør", required = true) @NotNull @Valid PersonIdent aktør,
                                 @JsonProperty(value = "dataset", required = true) @NotNull @Valid Set<Dataset> dataset) {
        this.saksnummer = saksnummer;
        this.nyReferanse = nyReferanse;
        this.gammelReferanse = gammelReferanse;
        this.ytelseType = ytelseType;
        this.aktør = aktør;
        this.dataset = dataset;
    }

    public UUID getNyReferanse() {
        return nyReferanse;
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

    public void setOpplysningsperiode(Periode opplysningsperiode) {
        this.opplysningsperiode = opplysningsperiode;
    }

    public Periode getOpptjeningsperiode() {
        return opptjeningsperiode;
    }

    public void setOpptjeningsperiode(Periode opptjeningsperiode) {
        this.opptjeningsperiode = opptjeningsperiode;
    }

    public Set<Dataset> getDataset() {
        return dataset;
    }

    public UUID getGammelReferanse() {
        return gammelReferanse;
    }

    public YtelseType getYtelseType() {
        return ytelseType;
    }

    public String getSaksnummer() {
        return saksnummer;
    }
}
