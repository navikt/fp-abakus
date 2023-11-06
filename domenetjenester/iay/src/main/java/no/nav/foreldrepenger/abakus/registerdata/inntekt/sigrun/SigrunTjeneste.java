package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunConsumer;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunResponse;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.pgifolketrygden.PgiFolketrygdenResponse;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.pgifolketrygden.SigrunPgiFolketrygdenMapper;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SSGResponse;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SigrunSummertSkattegrunnlagResponse;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.konfig.Environment;


@ApplicationScoped
public class SigrunTjeneste {
    private static final Logger LOG = LoggerFactory.getLogger(SigrunTjeneste.class);

    private static final boolean SAMMENLIGN_PGI = !Environment.current().isLocal();

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
                sammenlignPGI(fnr, justertOpplysningsperiode, resultat, summertSkattegrunnlag);
            }
        }
        return resultat;
    }

    private void sammenlignPGI(PersonIdent pi, IntervallEntitet opplysningsperiode,
                               Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> bs,
                               SigrunSummertSkattegrunnlagResponse summertSkattegrunnlag) {
        try {
            var nettoBs = bs.entrySet().stream().filter(e -> !e.getValue().isEmpty()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            var harSvalbardSSG = summertSkattegrunnlag.summertskattegrunnlagMap().values().stream()
                .flatMap(Optional::stream)
                .map(SSGResponse::svalbardGrunnlag)
                .mapToLong(Collection::size)
                .sum() > 0 ? "Svalbard SSG" : "";
            var pgiMap = sigrunConsumer.pgiFolketrygden(pi.getIdent(), opplysningsperiode);
            var harSvalbardPGI = pgiMap.pgiFolketrygdenMap().values().stream()
                .flatMap(Collection::stream)
                .map(PgiFolketrygdenResponse::pensjonsgivendeInntekt)
                .flatMap(Collection::stream)
                .anyMatch(v -> PgiFolketrygdenResponse.Skatteordning.SVALBARD.equals(v.skatteordning())) ? "Svalbard PGI" : "";
            var nettoPgi = SigrunPgiFolketrygdenMapper.mapFraSigrunTilIntern(pgiMap).entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            if (nettoBs.isEmpty() && nettoPgi.isEmpty()) {
                LOG.info("SIGRUN PGI: sammenlignet OK tomme svar fra begge {} {}", harSvalbardSSG, harSvalbardPGI);
            } else if (!nettoPgi.values().isEmpty() && nettoPgi.values().stream().anyMatch(v -> !v.values().isEmpty())) {
                if (sammenlignMaps(nettoBs, nettoPgi)) {
                    LOG.info("SIGRUN PGI: sammenlignet OK {} {}", harSvalbardSSG, harSvalbardPGI);
                } else {
                    LOG.info("SIGRUN PGI: sammenlignet DIFF {} {} BS/SSG {} PGI {} kilde {}", harSvalbardSSG, harSvalbardPGI, bs, nettoPgi, pgiMap);
                }
            } else if (!nettoBs.values().isEmpty() && nettoBs.values().stream().anyMatch(v -> !v.values().isEmpty())) {
                LOG.info("SIGRUN PGI: tomt svar fra PGI {} {} BS//SG {} kilde {}", harSvalbardSSG, harSvalbardPGI, bs, pgiMap);
            } else {
                LOG.info("SIGRUN PGI: tomme svar fra BS//SG {} kilde {}", bs, pgiMap);
            }
        } catch (Exception e) {
            LOG.info("SIGRUN PGI: noe gikk veldig galt", e);
        }
    }

    private static boolean sammenlignMaps(Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> bs, Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> pgi) {
        return Objects.equals(bs.keySet(), pgi.keySet()) && pgi.entrySet().stream().allMatch(e -> sammenlignSubMaps(bs.get(e.getKey()), e.getValue()));
    }

    private static boolean sammenlignSubMaps(Map<InntektspostType, BigDecimal> bs, Map<InntektspostType, BigDecimal> pgi) {
        return bs != null && Objects.equals(bs.keySet(), pgi.keySet()) && pgi.entrySet().stream().allMatch(e -> sammenlignSubMapVerdier(bs.get(e.getKey()), e.getValue()));
    }

    private static boolean sammenlignSubMapVerdier(BigDecimal bs, BigDecimal pgi) {
        return Objects.equals(bs, pgi) || (bs != null && pgi.compareTo(bs) == 0);
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
