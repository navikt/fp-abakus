package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.finn.unleash.Unleash;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.vedtak.felles.integrasjon.sigrun.BeregnetSkatt;
import no.nav.vedtak.felles.integrasjon.sigrun.SigrunConsumer;
import no.nav.vedtak.felles.integrasjon.sigrun.SigrunResponse;
import no.nav.vedtak.felles.integrasjon.sigrun.summertskattegrunnlag.SigrunSummertSkattegrunnlagResponse;


@ApplicationScoped
public class SigrunTjeneste {
    private static final Logger LOGGER = LoggerFactory.getLogger(SigrunTjeneste.class);

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
        SigrunResponse beregnetskatt;
        if (unleash.isEnabled("fpsak.sigrun.logg.respons", false)) {
            beregnetskatt = sigrunConsumer.beregnetskattMedLogging(Long.valueOf(aktørId.getId()));
            for (Map.Entry<Year, List<BeregnetSkatt>> entry : beregnetskatt.getBeregnetSkatt().entrySet()) {
                LOGGER.info("Beregnet skatt for " + entry.getKey() + " var: " + lagTekstAvRespons(entry.getValue()));
            }
        } else {
            beregnetskatt = sigrunConsumer.beregnetskatt(Long.valueOf(aktørId.getId()));
        }
        if (unleash.isEnabled("fpsak.sigrun.summertskattegrunnlag")) {
            SigrunSummertSkattegrunnlagResponse summertSkattegrunnlag = sigrunConsumer.summertSkattegrunnlag(Long.valueOf(aktørId.getId()));
            return SigrunTilInternMapper.mapFraSigrunTilIntern(beregnetskatt.getBeregnetSkatt(), summertSkattegrunnlag.getSummertskattegrunnlagMap());
        } else {
            return SigrunTilInternMapper.mapFraSigrunTilIntern(beregnetskatt.getBeregnetSkatt(), Collections.emptyMap());
        }
    }

    private static String lagTekstAvRespons(List<BeregnetSkatt> liste) {
        StringBuilder sb = new StringBuilder("BeregnetSkatt {");
        for (BeregnetSkatt skatt : liste) {
            sb.append(" tekniskNavn=").append(skatt.getTekniskNavn());
            sb.append(" verdi=").append(skatt.getVerdi());
        }
        sb.append(" }");
        return sb.toString();
    }
}
