package no.nav.foreldrepenger.abakus.kodeverk.tjeneste;

public class Kodeverk {

    private final String kodeverk;
    private final String kode;

    public Kodeverk(String kodeverk, String kode) {
        this.kodeverk = kodeverk;
        this.kode = kode;
    }

    public String getKodeverk() {
        return kodeverk;
    }

    public String getKode() {
        return kode;
    }
}
