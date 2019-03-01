package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon.tilleggsinformasjondetaljer;


public class ReiseKostOgLosji extends TilleggsinformasjonDetaljer {
    static final String TYPE = "REISEKOSTOGLOSJI";

    private String persontype;

    public String getPersontype() {
        return this.persontype;
    }

    public ReiseKostOgLosji(String persontype) {
        super(TilleggsinformasjonDetaljerType.REISEKOSTOGLOSJI);
        this.persontype = persontype;
    }

    public ReiseKostOgLosji() {
        super(TilleggsinformasjonDetaljerType.REISEKOSTOGLOSJI);
    }
}
