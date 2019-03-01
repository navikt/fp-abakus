package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon.tilleggsinformasjondetaljer;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AldersUfoereEtterlatteAvtalefestetOgKrigspensjon extends TilleggsinformasjonDetaljer {
    static final String TYPE = "ALDERSUFOEREETTERLATTEAVTALEFESTETOGKRIGSPENSJON";
    private BigDecimal grunnpensjonbeloep;
    private BigDecimal heravEtterlattepensjon;
    private Integer pensjonsgrad;
    private LocalDate tidsromFom;
    private LocalDate tidsromTom;
    private BigDecimal tilleggspensjonbeloep;
    private Integer ufoeregradpensjonsgrad;

    public BigDecimal getGrunnpensjonbeloep() {
        return this.grunnpensjonbeloep;
    }

    public BigDecimal getHeravEtterlattepensjon() {
        return this.heravEtterlattepensjon;
    }

    public Integer getPensjonsgrad() {
        return this.pensjonsgrad;
    }

    public LocalDate getTidsromFom() {
        return this.tidsromFom;
    }

    public LocalDate getTidsromTom() {
        return this.tidsromTom;
    }

    public BigDecimal getTilleggspensjonbeloep() {
        return this.tilleggspensjonbeloep;
    }

    public Integer getUfoeregradpensjonsgrad() {
        return this.ufoeregradpensjonsgrad;
    }

    public AldersUfoereEtterlatteAvtalefestetOgKrigspensjon(BigDecimal grunnpensjonbeloep, BigDecimal heravEtterlattepensjon, Integer pensjonsgrad, LocalDate tidsromFom, LocalDate tidsromTom, BigDecimal tilleggspensjonbeloep, Integer ufoeregradpensjonsgrad) {
        super(TilleggsinformasjonDetaljerType.ALDERSUFOEREETTERLATTEAVTALEFESTETOGKRIGSPENSJON);
        this.grunnpensjonbeloep = grunnpensjonbeloep;
        this.heravEtterlattepensjon = heravEtterlattepensjon;
        this.pensjonsgrad = pensjonsgrad;
        this.tidsromFom = tidsromFom;
        this.tidsromTom = tidsromTom;
        this.tilleggspensjonbeloep = tilleggspensjonbeloep;
        this.ufoeregradpensjonsgrad = ufoeregradpensjonsgrad;
    }

    public AldersUfoereEtterlatteAvtalefestetOgKrigspensjon() {
        super(TilleggsinformasjonDetaljerType.ALDERSUFOEREETTERLATTEAVTALEFESTETOGKRIGSPENSJON);
    }
}
