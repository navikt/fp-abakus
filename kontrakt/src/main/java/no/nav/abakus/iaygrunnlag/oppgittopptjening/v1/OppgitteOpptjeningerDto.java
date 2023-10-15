package no.nav.abakus.iaygrunnlag.oppgittopptjening.v1;

import java.util.Collections;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OppgitteOpptjeningerDto {

    @JsonProperty(value = "oppgitteOpptjeninger", required = true)
    @NotNull
    @Valid
    private List<OppgittOpptjeningDto> oppgitteOpptjeninger;

    public OppgitteOpptjeningerDto() {
        // default ctor
    }

    public List<OppgittOpptjeningDto> getOppgitteOpptjeninger() {
        return oppgitteOpptjeninger == null ? Collections.emptyList() : oppgitteOpptjeninger;
    }

    public void setOppgitteOpptjeninger(List<OppgittOpptjeningDto> oppgitteOpptjeninger) {
        this.oppgitteOpptjeninger = oppgitteOpptjeninger;
    }

    public OppgitteOpptjeningerDto medOppgitteOpptjeninger(List<OppgittOpptjeningDto> oppgitteOpptjeninger) {
        setOppgitteOpptjeninger(oppgitteOpptjeninger);
        return this;
    }
}
