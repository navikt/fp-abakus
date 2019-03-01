package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon.tilleggsinformasjondetaljer;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "detaljerType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = AldersUfoereEtterlatteAvtalefestetOgKrigspensjon.class, name = AldersUfoereEtterlatteAvtalefestetOgKrigspensjon.TYPE),
    @JsonSubTypes.Type(value = BarnepensjonOgUnderholdsbidrag.class, name = BarnepensjonOgUnderholdsbidrag.TYPE),
    @JsonSubTypes.Type(value = BonusFraForsvaret.class, name = BonusFraForsvaret.TYPE),
    @JsonSubTypes.Type(value = Etterbetalingsperiode.class, name = Etterbetalingsperiode.TYPE),
    @JsonSubTypes.Type(value = Inntjeningsforhold.class, name = Inntjeningsforhold.TYPE),
    @JsonSubTypes.Type(value = Svalbardinntekt.class, name = Svalbardinntekt.TYPE),
    @JsonSubTypes.Type(value = ReiseKostOgLosji.class, name = ReiseKostOgLosji.TYPE),
})
public abstract class TilleggsinformasjonDetaljer {
    private final TilleggsinformasjonDetaljerType detaljerType;


    public TilleggsinformasjonDetaljer(TilleggsinformasjonDetaljerType detaljerType) {
        this.detaljerType = detaljerType;
    }

    public TilleggsinformasjonDetaljerType getDetaljerType() {
        return detaljerType;
    }
}
