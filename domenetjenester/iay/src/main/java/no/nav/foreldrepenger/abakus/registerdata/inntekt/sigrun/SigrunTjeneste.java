package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun;

import java.math.BigDecimal;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunConsumer;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunNativeImpl;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunResponse;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SigrunSummertSkattegrunnlagResponse;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.vedtak.felles.integrasjon.rest.NativeClient;


@ApplicationScoped
public class SigrunTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(SigrunNativeImpl.class);

    private SigrunConsumer sigrunConsumer;
    private SigrunConsumer sigrunNativeConsumer;

    SigrunTjeneste() {
        //CDI
    }

    @Inject
    public SigrunTjeneste(SigrunConsumer sigrunConsumer, @NativeClient SigrunConsumer sigrunNativeConsumer) {
        this.sigrunConsumer = sigrunConsumer;
        this.sigrunNativeConsumer = sigrunNativeConsumer;
    }

    public Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> beregnetSkatt(AktørId aktørId) {
        SigrunResponse beregnetskatt = sigrunConsumer.beregnetskatt(Long.valueOf(aktørId.getId()));
        SigrunSummertSkattegrunnlagResponse summertSkattegrunnlag = sigrunConsumer.summertSkattegrunnlag(Long.valueOf(aktørId.getId()));
        var mapped = SigrunTilInternMapper.mapFraSigrunTilIntern(beregnetskatt.beregnetSkatt(), summertSkattegrunnlag.summertskattegrunnlagMap());
        compare(aktørId, mapped);
        return mapped;
    }

    private void compare(AktørId aktørId, Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> apache) {
        try {
            SigrunResponse beregnetskatt = sigrunNativeConsumer.beregnetskatt(Long.valueOf(aktørId.getId()));
            SigrunSummertSkattegrunnlagResponse summertSkattegrunnlag = sigrunNativeConsumer.summertSkattegrunnlag(Long.valueOf(aktørId.getId()));
            var mapped = SigrunTilInternMapper.mapFraSigrunTilIntern(beregnetskatt.beregnetSkatt(), summertSkattegrunnlag.summertskattegrunnlagMap());
            var like = apache.keySet().containsAll(mapped.keySet()) && mapped.keySet().containsAll(apache.keySet()) &&
                apache.entrySet().stream().anyMatch(e -> !erLike(e.getValue(), mapped.get(e.getKey())));
            LOG.info("Sigrun sammenligning resultat: {}", like ? "like" : "ulike");
        } catch (Exception e) {
            LOG.info("Sigrun sammenligning - noe gikk galt", e);
        }
    }

    private boolean erLike(Map<InntektspostType, BigDecimal> apache, Map<InntektspostType, BigDecimal> javahttp) {
        return apache.keySet().containsAll(javahttp.keySet()) && javahttp.keySet().containsAll(apache.keySet()) &&
            apache.entrySet().stream().anyMatch(e -> javahttp.get(e.getKey()) == null || e.getValue().compareTo(javahttp.get(e.getKey())) != 0);
    }
}
