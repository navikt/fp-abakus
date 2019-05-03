package no.nav.foreldrepenger.abakus.registerdata.tjeneste.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class SjekkStatusDto implements AbacDto {
    @Valid
    @NotNull
    private ReferanseDto referanse;

    @NotNull
    @Pattern(regexp = "\\d+")
    private String taskReferanse;

    public ReferanseDto getReferanse() {
        return referanse;
    }

    public void setReferanse(ReferanseDto referanse) {
        this.referanse = referanse;
    }

    public String getTaskReferanse() {
        return taskReferanse;
    }

    public void setTaskReferanse(String taskReferanse) {
        this.taskReferanse = taskReferanse;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett();
    }
}
