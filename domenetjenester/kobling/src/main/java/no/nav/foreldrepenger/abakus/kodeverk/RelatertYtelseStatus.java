package no.nav.foreldrepenger.abakus.kodeverk;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.iaygrunnlag.kodeverk.Kodeverdi;

public enum RelatertYtelseStatus implements Kodeverdi {

    // Statuser fra Arena
    AVSLU("AVSLU"),
    GODKJ("GODKJ"),
    INNST("INNST"),
    IVERK("IVERK"),
    MOTAT("MOTAT"),
    OPPRE("OPPRE"),
    REGIS("REGIS"),

    // Statuser far Infotrygd
    IKKE_PÅBEGYNT("IP"),
    UNDER_BEHANDLING("UB"),
    SENDT_TIL_SAKSBEHANDLER("SG"),
    UNDERKJENT_AV_SAKSBEHANDLER("UK"),
    RETUNERT("RT"),
    SENDT("ST"),
    VIDERESENDT_DIREKTORATET("VD"),
    VENTER_IVERKSETTING("VI"),
    VIDERESENDT_TRYGDERETTEN("VT"),

    LØPENDE_VEDTAK("L"),
    IKKE_STARTET("I"),
    AVSLUTTET_IT("A"),
    ;

    @JsonProperty(value = "kode")
    private String kode;

    @JsonProperty(value = "kodeverk")
    private String kodeverk = "RELATERT_YTELSE_STATUS";

    RelatertYtelseStatus(String kode) {
        this.kode = kode;
    }

    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getKodeverk() {
        return kodeverk;
    }

    @Override
    public String getOffisiellKode() {
        return kode;
    }

    @Override
    public String getNavn() {
        return kode;
    }
}
