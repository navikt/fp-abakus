package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient;

import static java.util.Arrays.asList;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SSGResponse;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SigrunSummertSkattegrunnlagResponse;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;

@ApplicationScoped
public class SigrunConsumerImpl implements SigrunConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SigrunConsumerImpl.class);

    private static final String TEKNISK_NAVN = "skatteoppgjoersdato";

    private static final MonthDay TIDLIGSTE_SJEKK_FJOR = MonthDay.of(5, 1);
    private static final boolean isProd = Environment.current().isProd();
    private final SigrunRestClient client;


    public SigrunConsumerImpl() {
        this.client = new SigrunRestClient(RestClient.client());

    }

    // Testformål
    SigrunConsumerImpl(SigrunRestClient restClient) {
        this.client = restClient;

    }

    @Override
    public SigrunResponse beregnetskatt(Long aktørId, IntervallEntitet opplysningsperiode) {
        var årTilListeMedSkatt = ferdiglignedeBeregnetSkattÅr(aktørId, opplysningsperiode).stream()
            .collect(Collectors.toMap(år -> år, år -> client.hentBeregnetSkattForAktørOgÅr(aktørId, år.toString())));
        return new SigrunResponse(årTilListeMedSkatt);
    }

    @Override
    public SigrunSummertSkattegrunnlagResponse summertSkattegrunnlag(Long aktørId, IntervallEntitet opplysningsperiode) {
        Map<Year, Optional<SSGResponse>> summertskattegrunnlagMap = hentÅrsListeForSummertskattegrunnlag(aktørId, opplysningsperiode).stream()
            .collect(Collectors.toMap(år -> år, år -> client.hentSummertskattegrunnlag(aktørId, år.toString())));
        return new SigrunSummertSkattegrunnlagResponse(summertskattegrunnlagMap);
    }

    private List<Year> ferdiglignedeBeregnetSkattÅr(Long aktørId, IntervallEntitet opplysningsperiode) {
        if (opplysningsperiode != null) {
            return beregnetSkattÅrslisteFraOpplysningsperiode(opplysningsperiode);
        } else {
            Year iFjor = Year.now().minusYears(1L);
            if (iFjorErFerdiglignetBeregnet(aktørId, iFjor)) {
                return asList(iFjor, iFjor.minusYears(1L), iFjor.minusYears(2L));
            } else {
                Year iForifjor = iFjor.minusYears(1L);
                return asList(iForifjor, iForifjor.minusYears(1L), iForifjor.minusYears(2L));
            }
        }
    }

    private List<Year> beregnetSkattÅrslisteFraOpplysningsperiode(IntervallEntitet opplysningsperiode) {
        var fomÅr = opplysningsperiode.getFomDato().getYear();
        var tomÅr = opplysningsperiode.getTomDato().getYear();
        var år = fomÅr;
        var årsListe = new ArrayList<Year>();
        while (år <= tomÅr) {
            årsListe.add(Year.of(år));
            år++;
        }
        return årsListe;
    }

    List<Year> hentÅrsListeForSummertskattegrunnlag(Long aktørId, IntervallEntitet opplysningsperiode) {
        Year iFjor = Year.now().minusYears(1L);
        if (opplysningsperiode != null) {
            var justertOpplysningsperiode = justerOpplysningsperiodeNårSisteÅrIkkeErFerdiglignet(aktørId, opplysningsperiode);
            return summertSkattegrunnlagÅrslisteFraOpplysningsperiode(justertOpplysningsperiode);
        } else {
            //filteret(SummertSkattegrunnlagForeldrepenger) i Sigrun er ikke impl. tidligere enn 2018
            if (iFjor.equals(Year.of(2018))) {
                return List.of(iFjor);
            } else if (iFjor.equals(Year.of(2019))) {
                return List.of(iFjor, iFjor.minusYears(1L));
            }
            return ferdiglignedeBeregnetSkattÅr(aktørId, opplysningsperiode);
        }
    }

    private IntervallEntitet justerOpplysningsperiodeNårSisteÅrIkkeErFerdiglignet(Long aktørId, IntervallEntitet opplysningsperiode) {
        // justerer slik at vi henter ett å eldre data når siste år som etterspørs ikke er ferdiglignet enda
        int fomÅr = opplysningsperiode.getFomDato().getYear();
        int tomÅr = opplysningsperiode.getTomDato().getYear();
        Year iFjor = Year.now().minusYears(1);
        logger.info("Opprinnelig opplysningsperiode er fom {} tom {}", fomÅr, tomÅr);
        if (opplysningsperiode.getTomDato().getYear() == iFjor.getValue()) {
            boolean fjordårdetFerdiglignet = iFjorErFerdiglignetBeregnet(aktørId, iFjor);
            logger.info("Ferdiglignet {}", fjordårdetFerdiglignet);
            if (!fjordårdetFerdiglignet && tomÅr - fomÅr < 3) { //dataminimering, ikke behov for data ut over 3 hele år før første stp
                logger.info("Utvider opplysningsperioden med ett år pga ikke-ferdiglignet år");
                fomÅr--;
            }
        }
        LocalDate fom = LocalDate.of(fomÅr, 1, 1);
        LocalDate tom = LocalDate.of(tomÅr, 12, 31);
        return IntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }

    private ArrayList<Year> summertSkattegrunnlagÅrslisteFraOpplysningsperiode(IntervallEntitet opplysningsperiode) {
        var fomÅr = opplysningsperiode.getFomDato().getYear();
        var tomÅr = opplysningsperiode.getTomDato().getYear();
        var år = fomÅr;
        var årsListe = new ArrayList<Year>();
        while (år <= tomÅr) {
            //filteret(SummertSkattegrunnlagForeldrepenger) i Sigrun er ikke impl. tidligere enn 2018
            if (år >= 2018) {
                årsListe.add(Year.of(år));
            }
            år++;
        }
        return årsListe;
    }

    private boolean iFjorErFerdiglignetBeregnet(Long aktørId, Year iFjor) {
        if (isProd && Year.now().minusYears(1).equals(iFjor) && MonthDay.now().isBefore(TIDLIGSTE_SJEKK_FJOR)) {
            return false;
        }
        return client.hentBeregnetSkattForAktørOgÅr(aktørId, iFjor.toString()).stream().anyMatch(l -> l.tekniskNavn().equals(TEKNISK_NAVN));
    }


}
