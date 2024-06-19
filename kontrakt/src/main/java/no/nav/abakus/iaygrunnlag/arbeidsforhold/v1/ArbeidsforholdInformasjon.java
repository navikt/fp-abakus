package no.nav.abakus.iaygrunnlag.arbeidsforhold.v1;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import no.nav.abakus.iaygrunnlag.v1.InntektArbeidYtelseGrunnlagDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class ArbeidsforholdInformasjon {

    @JsonProperty(value = "referanser")
    @Valid
    private List<ArbeidsforholdReferanseDto> referanser;

    @JsonProperty(value = "overstyringer")
    @Valid
    private List<ArbeidsforholdOverstyringDto> overstyringer;

    /**
     * Grunnlag referanse, samme som {@link InntektArbeidYtelseGrunnlagDto#getGrunnlagReferanse()} men nyttig når
     * returnerer denne DTO alene.
     */
    @JsonProperty(value = "grunnlagReferanse")
    private UUID grunnlagRef;

    public ArbeidsforholdInformasjon() {}

    public ArbeidsforholdInformasjon(UUID grunnlagRef) {
        this.grunnlagRef = grunnlagRef;
    }

    public List<ArbeidsforholdReferanseDto> getReferanser() {
        return referanser;
    }

    public void setReferanser(List<ArbeidsforholdReferanseDto> referanser) {
        this.referanser = referanser;
    }

    public ArbeidsforholdInformasjon medReferanser(List<ArbeidsforholdReferanseDto> referanser) {
        this.referanser = referanser;
        return this;
    }

    public UUID getGrunnlagRef() {
        return this.grunnlagRef;
    }

    public List<ArbeidsforholdOverstyringDto> getOverstyringer() {
        return overstyringer;
    }

    public void setOverstyringer(List<ArbeidsforholdOverstyringDto> overstyringer) {
        this.overstyringer = overstyringer;
    }

    public ArbeidsforholdInformasjon medOverstyringer(List<ArbeidsforholdOverstyringDto> overstyringer) {
        this.overstyringer = overstyringer;
        return this;
    }
}
