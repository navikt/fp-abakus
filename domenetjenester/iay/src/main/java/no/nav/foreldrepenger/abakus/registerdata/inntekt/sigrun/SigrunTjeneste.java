package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunConsumer;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunResponse;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SigrunSummertSkattegrunnlagResponse;
import no.nav.foreldrepenger.abakus.typer.AktørId;


@ApplicationScoped
public class SigrunTjeneste {
    private static final Logger logger = LoggerFactory.getLogger(SigrunTjeneste.class);

    private SigrunConsumer sigrunConsumer;

    SigrunTjeneste() {
        //CDI
    }

    @Inject
    public SigrunTjeneste(SigrunConsumer sigrunConsumer) {
        this.sigrunConsumer = sigrunConsumer;
    }


    public Map<IntervallEntitet, Map<InntektspostType, BigDecimal>> beregnetSkatt(AktørId aktørId,
                                                                                  IntervallEntitet opplysningsperiodeSkattegrunnlag) {

        var justertOpplysningsperiode = justerOpplysningsperiodeNårSisteÅrIkkeErFerdiglignet(Long.valueOf(aktørId.getId()), opplysningsperiodeSkattegrunnlag);
        SigrunResponse beregnetskatt = sigrunConsumer.beregnetskatt(Long.valueOf(aktørId.getId()), justertOpplysningsperiode);
        SigrunSummertSkattegrunnlagResponse summertSkattegrunnlag = sigrunConsumer.summertSkattegrunnlag(Long.valueOf(aktørId.getId()),
            opplysningsperiodeSkattegrunnlag);
        return SigrunTilInternMapper.mapFraSigrunTilIntern(beregnetskatt.beregnetSkatt(), summertSkattegrunnlag.summertskattegrunnlagMap());
    }

    IntervallEntitet justerOpplysningsperiodeNårSisteÅrIkkeErFerdiglignet(Long aktørId, IntervallEntitet opplysningsperiode) {
        if (opplysningsperiode == null){
            return null; //fpsak spør unten å oppgi periode
        }
        // justerer slik at vi henter ett å eldre data når siste år som etterspørs ikke er ferdiglignet enda
        int fomÅr = opplysningsperiode.getFomDato().getYear();
        int tomÅr = opplysningsperiode.getTomDato().getYear();
        Year iFjor = Year.now().minusYears(1);
        logger.info("Opprinnelig opplysningsperiode er fom {} tom {}", fomÅr, tomÅr);
        if (opplysningsperiode.getTomDato().getYear() == iFjor.getValue()) {
            boolean fjoråretFerdiglignet = sigrunConsumer.erÅretFerdiglignet(aktørId, iFjor);
            logger.info("Ferdiglignet {}", fjoråretFerdiglignet);
            if (!fjoråretFerdiglignet && tomÅr - fomÅr < 3) { //dataminimering, ikke behov for data ut over 3 hele år før første stp
                logger.info("Utvider opplysningsperioden med ett år pga ikke-ferdiglignet år");
                return IntervallEntitet.fraOgMedTilOgMed(opplysningsperiode.getFomDato().minusYears(1), opplysningsperiode.getTomDato());
            }
        }
        return opplysningsperiode;
    }

}
