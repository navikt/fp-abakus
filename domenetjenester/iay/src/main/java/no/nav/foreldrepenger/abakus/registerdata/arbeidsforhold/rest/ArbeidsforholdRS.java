package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ArbeidsforholdRS {

    @JsonProperty("arbeidsforholdId")
    private String arbeidsforholdId;
    @JsonProperty("navArbeidsforholdId")
    private Long navArbeidsforholdId;
    @JsonProperty("arbeidsgiver")
    private OpplysningspliktigArbeidsgiverRS arbeidsgiver;
    @JsonProperty("ansettelsesperiode")
    private AnsettelsesperiodeRS ansettelsesperiode;
    @JsonProperty("arbeidsavtaler")
    private List<ArbeidsavtaleRS> arbeidsavtaler;
    @JsonProperty("permisjonPermitteringer")
    private List<PermisjonPermitteringRS> permisjonPermitteringer;
    @JsonProperty("type")
    private String type; // (kodeverk: Arbeidsforholdtyper)

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public Long getNavArbeidsforholdId() {
        return navArbeidsforholdId;
    }

    public OpplysningspliktigArbeidsgiverRS getArbeidsgiver() {
        return arbeidsgiver;
    }

    public AnsettelsesperiodeRS getAnsettelsesperiode() {
        return ansettelsesperiode;
    }

    public List<ArbeidsavtaleRS> getArbeidsavtaler() {
        return arbeidsavtaler;
    }

    public List<PermisjonPermitteringRS> getPermisjonPermitteringer() {
        return permisjonPermitteringer;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ArbeidsforholdRS{" +
                "arbeidsforholdId='" + arbeidsforholdId + '\'' +
                ", navArbeidsforholdId=" + navArbeidsforholdId +
                ", arbeidsgiver=" + arbeidsgiver +
                ", ansettelsesperiode=" + ansettelsesperiode +
                ", arbeidsavtaler=" + arbeidsavtaler +
                ", permisjonPermitteringer=" + permisjonPermitteringer +
                ", type='" + type + '\'' +
                '}';
    }
}
