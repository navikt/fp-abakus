package no.nav.foreldrepenger.abakus.lonnskomp.kafka;

public class LønnskompensasjonVedtakMelding {
    private String fnr;
    private String totalKompensasjon;
    private String bedriftNr;
    private String fom;
    private String tom;
    private String sakId;
    private boolean avslag;

    private String forrigeVedtakDato;

    public String getFnr() {
        return fnr;
    }

    public String getTotalKompensasjon() {
        return totalKompensasjon;
    }

    public String getBedriftNr() {
        return bedriftNr;
    }

    public String getFom() {
        return fom;
    }

    public String getTom() {
        return tom;
    }

    public String getSakId() {
        return sakId;
    }

    public String getForrigeVedtakDato() {
        return forrigeVedtakDato;
    }

    public boolean isAvslag() {
        return avslag;
    }

}
