package no.nav.abakus.iaygrunnlag.inntektsmelding.v1;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class RefusjonskravDatoerDto {

    @JsonProperty(value = "refusjonskravDatoer", required = true)
    @NotNull
    @Valid
    private List<RefusjonskravDatoDto> refusjonskravDatoer;

    public RefusjonskravDatoerDto() {
        // default ctor
    }

    public RefusjonskravDatoerDto(@NotNull @Valid List<RefusjonskravDatoDto> refusjonskravDatoer) {
        this.refusjonskravDatoer = refusjonskravDatoer;
    }

    public List<RefusjonskravDatoDto> getRefusjonskravDatoer() {
        return refusjonskravDatoer;
    }

}
