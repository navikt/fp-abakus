package no.nav.abakus.iaygrunnlag.inntektsmelding.v1;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class InntektsmeldingerDto {

    @JsonProperty(value = "inntektsmeldinger", required = true)
    @NotNull
    @Valid
    private List<InntektsmeldingDto> inntektsmeldinger;

    public InntektsmeldingerDto() {
        // default ctor
    }

    public List<InntektsmeldingDto> getInntektsmeldinger() {
        return inntektsmeldinger == null ? Collections.emptyList() : inntektsmeldinger;
    }

    public void setInntektsmeldinger(List<InntektsmeldingDto> inntektsmeldinger) {
        this.inntektsmeldinger = inntektsmeldinger;
    }

    public InntektsmeldingerDto medInntektsmeldinger(List<InntektsmeldingDto> inntektsmeldinger) {
        setInntektsmeldinger(inntektsmeldinger);
        return this;
    }
}
