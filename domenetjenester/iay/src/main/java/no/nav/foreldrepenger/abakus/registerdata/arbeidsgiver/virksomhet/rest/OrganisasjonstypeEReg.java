package no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.rest;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OrganisasjonstypeEReg {
    JURIDISK_ENHET("JuridiskEnhet"),
    VIRKSOMHET("Virksomhet"),
    ORGLEDD("Organisasjonsledd")
    ;

    private final String kode;

    @JsonValue
    public String getKode() {
        return this.kode;
    }

    private OrganisasjonstypeEReg(String kode) {
        this.kode = kode;
    }
}
