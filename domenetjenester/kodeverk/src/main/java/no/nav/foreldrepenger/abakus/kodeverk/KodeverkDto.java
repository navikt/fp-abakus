package no.nav.foreldrepenger.abakus.kodeverk;

public class KodeverkDto {

    private String kodeverk;
    private String kode;

    public KodeverkDto(String kodeverk, String kode) {
        this.kodeverk = kodeverk;
        this.kode = kode;
    }

    public KodeverkDto() {
    }

    public String getKodeverk() {
        return kodeverk;
    }

    public void setKodeverk(String kodeverk) {
        this.kodeverk = kodeverk;
    }

    public String getKode() {
        return kode;
    }

    public void setKode(String kode) {
        this.kode = kode;
    }
}
