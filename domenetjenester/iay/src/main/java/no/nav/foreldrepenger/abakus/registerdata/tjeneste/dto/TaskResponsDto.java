package no.nav.foreldrepenger.abakus.registerdata.tjeneste.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
public class TaskResponsDto {

    @JsonProperty(value = "taskGruppe", required = true)
    @NotNull
    @Valid
    private String taskGruppe;

    public TaskResponsDto(String taskGruppe) {
        this.taskGruppe = taskGruppe;
    }

    public String getTaskGruppe() {
        return taskGruppe;
    }
}
