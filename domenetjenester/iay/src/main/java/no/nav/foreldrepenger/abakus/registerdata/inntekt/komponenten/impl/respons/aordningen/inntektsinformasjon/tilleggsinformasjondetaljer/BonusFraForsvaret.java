package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon.tilleggsinformasjondetaljer;

import java.time.Year;

public class BonusFraForsvaret extends TilleggsinformasjonDetaljer {
    static final String TYPE = "BONUSFRAFORSVARET";

    private Year aaretUtbetalingenGjelderFor;

    public Year getAaretUtbetalingenGjelderFor() {
        return this.aaretUtbetalingenGjelderFor;
    }

    public BonusFraForsvaret(Year aaretUtbetalingenGjelderFor) {
        super(TilleggsinformasjonDetaljerType.BONUSFRAFORSVARET);
        this.aaretUtbetalingenGjelderFor = aaretUtbetalingenGjelderFor;
    }

    public BonusFraForsvaret() {
        super(TilleggsinformasjonDetaljerType.BONUSFRAFORSVARET);
    }
}
