package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun;

import java.math.BigDecimal;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.vedtak.felles.integrasjon.sigrun.SigrunConsumer;
import no.nav.vedtak.felles.integrasjon.sigrun.SigrunResponse;
import no.nav.vedtak.felles.integrasjon.sigrun.summertskattegrunnlag.SigrunSummertSkattegrunnlagResponse;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;


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

    public Map<DatoIntervallEntitet, Map<InntektspostType, BigDecimal>> beregnetSkatt(AktørId aktørId) {
        SigrunResponse beregnetskatt = sigrunConsumer.beregnetskatt(Long.valueOf(aktørId.getId()));
        SigrunSummertSkattegrunnlagResponse summertSkattegrunnlag = sigrunConsumer.summertSkattegrunnlag(Long.valueOf(aktørId.getId()));

        return SigrunTilInternMapper.mapFraSigrunTilIntern(beregnetskatt.getBeregnetSkatt(), summertSkattegrunnlag.getSummertskattegrunnlagMap());
    }
}
