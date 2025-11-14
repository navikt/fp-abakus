package no.nav.abakus.iaygrunnlag.arbeidsforhold.v1;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.abakus.iaygrunnlag.Aktør;
import no.nav.abakus.iaygrunnlag.ArbeidsforholdRefDto;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.arbeid.v1.PermisjonDto;
import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class ArbeidsforholdDto {

    @JsonProperty(value = "arbeidsgiver", required = true)
    @Valid
    @NotNull
    private Aktør arbeidsgiver;

    @JsonProperty(value = "arbeidType", required = true)
    @NotNull
    private ArbeidType type;

    @JsonProperty("arbeidsforholdId")
    @Valid
    private ArbeidsforholdRefDto arbeidsforholdId;

    @JsonProperty("ansettelsePerioder")
    @Valid
    private List<Periode> ansettelsesperiode;
    @JsonProperty("arbeidsavtaler")
    @Valid
    private List<ArbeidsavtaleDto> arbeidsavtaler;
    @JsonProperty("permisjoner")
    @Valid
    private List<PermisjonDto> permisjoner;

    protected ArbeidsforholdDto() {
    }

    public ArbeidsforholdDto(Aktør arbeidsgiver, ArbeidType type) {
        this.arbeidsgiver = arbeidsgiver;
        this.type = type;
    }

    public Aktør getArbeidsgiver() {
        return arbeidsgiver;
    }

    public ArbeidsforholdRefDto getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public void setArbeidsforholdId(ArbeidsforholdRefDto arbeidsforholdId) {
        this.arbeidsforholdId = arbeidsforholdId;
    }

    public ArbeidType getType() {
        return type;
    }

    public List<Periode> getAnsettelsesperiode() {
        return ansettelsesperiode;
    }
    public List<PermisjonDto> getPermisjoner() {
        return permisjoner;
    }
    public List<ArbeidsavtaleDto> getArbeidsavtaler() {
        return arbeidsavtaler;
    }

    public void setAnsettelsesperiode(List<Periode> ansettelsesperiode) {
        this.ansettelsesperiode = ansettelsesperiode;
    }
    public void setArbeidsavtaler(List<ArbeidsavtaleDto> arbeidsavtaler) {
        this.arbeidsavtaler = arbeidsavtaler;
    }
    public void setPermisjoner(List<PermisjonDto> permisjoner) {
        this.permisjoner = permisjoner;
    }
}
