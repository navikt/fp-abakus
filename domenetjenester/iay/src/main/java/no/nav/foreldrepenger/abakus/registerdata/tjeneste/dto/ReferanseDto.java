package no.nav.foreldrepenger.abakus.registerdata.tjeneste.dto;

public class ReferanseDto {

    private String referanse;

    public ReferanseDto(String referanse) {
        this.referanse = referanse;
    }

    public ReferanseDto() {
    }

    public String getReferanse() {
        return referanse;
    }

    public void setReferanse(String referanse) {
        this.referanse = referanse;
    }
}
