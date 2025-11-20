package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps;

public enum RelatertYtelseStatus {

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

    private String kode;

    RelatertYtelseStatus(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }

}
