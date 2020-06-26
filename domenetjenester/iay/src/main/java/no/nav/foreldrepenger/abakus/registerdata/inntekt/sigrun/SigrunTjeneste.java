package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun;

import no.finn.unleash.Unleash;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunConsumer;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunResponse;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SigrunSummertSkattegrunnlagResponse;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;


@ApplicationScoped
public class SigrunTjeneste {

    private SigrunConsumer sigrunConsumer;
    private Unleash unleash;

    SigrunTjeneste() {
        //CDI
    }

    @Inject
    public SigrunTjeneste(SigrunConsumer sigrunConsumer, Unleash unleash) {
        this.sigrunConsumer = sigrunConsumer;
        this.unleash = unleash;
    }

    public Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> beregnetSkatt(AktørId aktørId) {
        SigrunResponse beregnetskatt = sigrunConsumer.beregnetskatt(Long.valueOf(aktørId.getId()));
        if (unleash.isEnabled("fpsak.sigrun.summertskattegrunnlag")) {
            SigrunSummertSkattegrunnlagResponse summertSkattegrunnlag = sigrunConsumer.summertSkattegrunnlag(Long.valueOf(aktørId.getId()));
            return SigrunTilInternMapper.mapFraSigrunTilIntern(beregnetskatt.getBeregnetSkatt(), summertSkattegrunnlag.getSummertskattegrunnlagMap());
        } else {
            return SigrunTilInternMapper.mapFraSigrunTilIntern(beregnetskatt.getBeregnetSkatt(), Collections.emptyMap());
        }
    }
}
