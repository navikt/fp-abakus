package no.nav.foreldrepenger.abakus.iay.tjeneste.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class HentDiffGrunnlagDto {

    @NotNull
    @Valid
    private ReferanseDto gammelReferanse;
    @NotNull
    @Valid
    private ReferanseDto nyReferanse;

    public HentDiffGrunnlagDto() {
    }

    public HentDiffGrunnlagDto(ReferanseDto gammelReferanse, ReferanseDto nyReferanse) {
        this.gammelReferanse = gammelReferanse;
        this.nyReferanse = nyReferanse;
    }

    public ReferanseDto getGammelReferanse() {
        return gammelReferanse;
    }

    public void setGammelReferanse(ReferanseDto gammelReferanse) {
        this.gammelReferanse = gammelReferanse;
    }

    public ReferanseDto getNyReferanse() {
        return nyReferanse;
    }

    public void setNyReferanse(ReferanseDto nyReferanse) {
        this.nyReferanse = nyReferanse;
    }
}
