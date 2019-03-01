package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon.tilleggsinformasjondetaljer;

import java.math.BigDecimal;

public class Svalbardinntekt extends TilleggsinformasjonDetaljer {
    static final String TYPE = "SVALBARDINNTEKT";
    private Integer antallDager;
    private BigDecimal betaltTrygdeavgift;

    public Integer getAntallDager() {
        return this.antallDager;
    }

    public BigDecimal getBetaltTrygdeavgift() {
        return this.betaltTrygdeavgift;
    }

    public Svalbardinntekt(Integer antallDager, BigDecimal betaltTrygdeavgift) {
        super(TilleggsinformasjonDetaljerType.SVALBARDINNTEKT);
        this.antallDager = antallDager;
        this.betaltTrygdeavgift = betaltTrygdeavgift;
    }

    public Svalbardinntekt() {
        super(TilleggsinformasjonDetaljerType.SVALBARDINNTEKT);
    }
}
