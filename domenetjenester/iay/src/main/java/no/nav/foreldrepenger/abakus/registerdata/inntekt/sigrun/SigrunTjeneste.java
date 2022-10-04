package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun;

import java.math.BigDecimal;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunConsumer;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunResponse;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SigrunSummertSkattegrunnlagResponse;
import no.nav.foreldrepenger.abakus.typer.AktørId;


@ApplicationScoped
public class SigrunTjeneste {

    private SigrunConsumer sigrunConsumer;

    SigrunTjeneste() {
        //CDI
    }

    @Inject
    public SigrunTjeneste(SigrunConsumer sigrunConsumer) {
        this.sigrunConsumer = sigrunConsumer;
    }

    public Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> beregnetSkatt(AktørId aktørId) {
        SigrunResponse beregnetskatt = sigrunConsumer.beregnetskatt(Long.valueOf(aktørId.getId()));
        SigrunSummertSkattegrunnlagResponse summertSkattegrunnlag = sigrunConsumer.summertSkattegrunnlag(Long.valueOf(aktørId.getId()));
        return SigrunTilInternMapper.mapFraSigrunTilIntern(beregnetskatt.beregnetSkatt(), summertSkattegrunnlag.summertskattegrunnlagMap());
    }

}
