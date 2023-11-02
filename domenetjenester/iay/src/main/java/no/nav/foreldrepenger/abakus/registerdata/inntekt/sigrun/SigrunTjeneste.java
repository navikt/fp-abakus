package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunConsumer;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunResponse;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.pgifolketrygden.SigrunPgiFolketrygdenMapper;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SigrunSummertSkattegrunnlagResponse;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.konfig.Environment;


@ApplicationScoped
public class SigrunTjeneste {
    private static final Logger LOG = LoggerFactory.getLogger(SigrunTjeneste.class);

    private static final boolean SAMMENLIGN_PGI = Environment.current().isDev();

    private SigrunConsumer sigrunConsumer;

    SigrunTjeneste() {
        //CDI
    }

    @Inject
    public SigrunTjeneste(SigrunConsumer sigrunConsumer) {
        this.sigrunConsumer = sigrunConsumer;
    }


    public Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> beregnetSkatt(AktørId aktørId, Supplier<PersonIdent> fnrSupplier, IntervallEntitet opplysningsperiodeSkattegrunnlag) {

        var justertOpplysningsperiode = justerOpplysningsperiodeNårSisteÅrIkkeErFerdiglignet(Long.valueOf(aktørId.getId()), opplysningsperiodeSkattegrunnlag);
        SigrunResponse beregnetskatt = sigrunConsumer.beregnetskatt(Long.valueOf(aktørId.getId()), justertOpplysningsperiode);
        SigrunSummertSkattegrunnlagResponse summertSkattegrunnlag = sigrunConsumer.summertSkattegrunnlag(Long.valueOf(aktørId.getId()),
            opplysningsperiodeSkattegrunnlag);
        var resultat = SigrunTilInternMapper.mapFraSigrunTilIntern(beregnetskatt.beregnetSkatt(), summertSkattegrunnlag.summertskattegrunnlagMap());
        if (SAMMENLIGN_PGI) {
            var fnr = fnrSupplier.get();
            if (fnr != null) {
                sammenlignPGI(fnr, justertOpplysningsperiode, resultat);
            }
        }
        return resultat;
    }

    private void sammenlignPGI(PersonIdent pi, IntervallEntitet opplysningsperiode, Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> bs) {
        try {
            var pgiMap = sigrunConsumer.pgiFolketrygden(pi.getIdent(), opplysningsperiode);
            var pgiIntern = SigrunPgiFolketrygdenMapper.mapFraSigrunTilIntern(pgiMap);
            if (!pgiIntern.values().isEmpty() && pgiIntern.values().stream().anyMatch(v -> !v.values().isEmpty())) {
                if (sammenlignMaps(bs, pgiIntern)) {
                    LOG.info("SIGRUN PGI: sammenlignet OK");
                } else {
                    LOG.info("SIGRUN PGI: sammenlignet diff bs {} pgi {} kilde {}", bs, pgiIntern, pgiMap);
                }
            } else {
                LOG.info("SIGRUN PGI: tomt svar fra PGI");
            }
        } catch (Exception e) {
            LOG.info("SIGRUN PGI: noe gikk veldig galt", e);
        }
    }

    private boolean sammenlignMaps(Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> bs, Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> pgi) {
        return bs.keySet() == pgi.keySet() && pgi.entrySet().stream()
            .allMatch(e -> e.getValue().keySet() == bs.get(e.getKey()).keySet() &&
                e.getValue().entrySet().stream().allMatch(e2 -> Objects.equals(e2.getValue(), bs.get(e.getKey()).get(e2.getKey()))));
    }

    IntervallEntitet justerOpplysningsperiodeNårSisteÅrIkkeErFerdiglignet(Long aktørId, IntervallEntitet opplysningsperiode) {
        if (opplysningsperiode == null){
            return null; //fpsak spør unten å oppgi periode
        }
        // justerer slik at vi henter ett å eldre data når siste år som etterspørs ikke er ferdiglignet enda
        int fomÅr = opplysningsperiode.getFomDato().getYear();
        int tomÅr = opplysningsperiode.getTomDato().getYear();
        Year iFjor = Year.now().minusYears(1);
        LOG.info("Opprinnelig opplysningsperiode er fom {} tom {}", fomÅr, tomÅr);
        if (opplysningsperiode.getTomDato().getYear() == iFjor.getValue()) {
            boolean fjoråretFerdiglignet = sigrunConsumer.erÅretFerdiglignet(aktørId, iFjor);
            LOG.info("Ferdiglignet {}", fjoråretFerdiglignet);
            if (!fjoråretFerdiglignet && tomÅr - fomÅr < 3) { //dataminimering, ikke behov for data ut over 3 hele år før første stp
                LOG.info("Utvider opplysningsperioden med ett år pga ikke-ferdiglignet år");
                return IntervallEntitet.fraOgMedTilOgMed(opplysningsperiode.getFomDato().minusYears(1), opplysningsperiode.getTomDato());
            }
        }
        return opplysningsperiode;
    }

}
