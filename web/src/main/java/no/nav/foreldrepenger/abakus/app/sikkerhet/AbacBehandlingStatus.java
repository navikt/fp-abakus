package no.nav.foreldrepenger.abakus.app.sikkerhet;

public enum AbacBehandlingStatus {
    OPPRETTET("Opprettet"),
    UTREDES("Behandling utredes"),
    FATTE_VEDTAK("Kontroller og fatte vedtak");

    private String eksternKode;

    AbacBehandlingStatus(String eksternKode) {
        this.eksternKode = eksternKode;
    }

    public String getEksternKode() {
        return eksternKode;
    }
}
