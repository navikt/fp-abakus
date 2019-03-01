package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon.tilleggsinformasjondetaljer;

import java.time.LocalDate;

public class BarnepensjonOgUnderholdsbidrag extends TilleggsinformasjonDetaljer {
    static final String TYPE = "BARNEPENSJONOGUNDERHOLDSBIDRAG";
    private String forsoergersFoedselnummer;
    private LocalDate tidsromFom;
    private LocalDate tidsromTom;

    public String getForsoergersFoedselnummer() {
        return this.forsoergersFoedselnummer;
    }

    public LocalDate getTidsromFom() {
        return this.tidsromFom;
    }

    public LocalDate getTidsromTom() {
        return this.tidsromTom;
    }

    public BarnepensjonOgUnderholdsbidrag(String forsoergersFoedselnummer, LocalDate tidsromFom, LocalDate tidsromTom) {
        super(TilleggsinformasjonDetaljerType.BARNEPENSJONOGUNDERHOLDSBIDRAG);
        this.forsoergersFoedselnummer = forsoergersFoedselnummer;
        this.tidsromFom = tidsromFom;
        this.tidsromTom = tidsromTom;
    }

    public BarnepensjonOgUnderholdsbidrag() {
        super(TilleggsinformasjonDetaljerType.BARNEPENSJONOGUNDERHOLDSBIDRAG);
    }
}
