package no.nav.foreldrepenger.abakus.registerdata.tjeneste.dto;

import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;

public class YtelseTypeKodeverk {

    private String kode;
    private final String kodeverk = YtelseType.DISCRIMINATOR;

    public String getKode() {
        return kode;
    }

    public void setKode(String kode) {
        this.kode = kode;
    }

    public String getKodeverk() {
        return kodeverk;
    }
}
