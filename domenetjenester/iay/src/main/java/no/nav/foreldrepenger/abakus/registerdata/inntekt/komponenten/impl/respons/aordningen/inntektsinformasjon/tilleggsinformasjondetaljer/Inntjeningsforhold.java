package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon.tilleggsinformasjondetaljer;


public class Inntjeningsforhold extends TilleggsinformasjonDetaljer {
    static final String TYPE = "INNTJENINGSFORHOLD";

    private String spesielleInntjeningsforhold;

    public String getSpesielleInntjeningsforhold() {
        return this.spesielleInntjeningsforhold;
    }

    public Inntjeningsforhold(String spesielleInntjeningsforhold) {
        super(TilleggsinformasjonDetaljerType.INNTJENINGSFORHOLD);
        this.spesielleInntjeningsforhold = spesielleInntjeningsforhold;
    }

    public Inntjeningsforhold() {
        super(TilleggsinformasjonDetaljerType.INNTJENINGSFORHOLD);
    }
}
