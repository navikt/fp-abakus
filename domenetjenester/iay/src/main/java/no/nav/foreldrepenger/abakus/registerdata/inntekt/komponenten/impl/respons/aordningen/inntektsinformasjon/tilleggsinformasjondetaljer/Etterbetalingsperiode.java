package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon.tilleggsinformasjondetaljer;

import java.time.LocalDate;

public class Etterbetalingsperiode extends TilleggsinformasjonDetaljer {
    static final String TYPE = "ETTERBETALINGSPERIODE";
    private LocalDate etterbetalingsperiodeFom;
    private LocalDate etterbetalingsperiodeTom;

    public LocalDate getEtterbetalingsperiodeFom() {
        return this.etterbetalingsperiodeFom;
    }

    public LocalDate getEtterbetalingsperiodeTom() {
        return this.etterbetalingsperiodeTom;
    }

    public Etterbetalingsperiode(LocalDate etterbetalingsperiodeFom, LocalDate etterbetalingsperiodeTom) {
        super(TilleggsinformasjonDetaljerType.ETTERBETALINGSPERIODE);
        this.etterbetalingsperiodeFom = etterbetalingsperiodeFom;
        this.etterbetalingsperiodeTom = etterbetalingsperiodeTom;
    }

    public Etterbetalingsperiode() {
        super(TilleggsinformasjonDetaljerType.ETTERBETALINGSPERIODE);
    }
}
