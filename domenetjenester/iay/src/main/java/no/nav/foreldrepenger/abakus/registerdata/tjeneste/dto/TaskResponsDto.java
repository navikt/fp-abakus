package no.nav.foreldrepenger.abakus.registerdata.tjeneste.dto;

public class TaskResponsDto {
    private String taskGruppe;

    public TaskResponsDto(String taskGruppe) {
        this.taskGruppe = taskGruppe;
    }

    public String getTaskGruppe() {
        return taskGruppe;
    }
}
