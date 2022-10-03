package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient;

import static java.util.Arrays.asList;

import java.time.MonthDay;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SSGResponse;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SigrunSummertSkattegrunnlagResponse;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;

@ApplicationScoped
public class SigrunConsumerImpl implements SigrunConsumer {

    private static final String TEKNISK_NAVN = "skatteoppgjoersdato";

    private static final MonthDay TIDLIGSTE_SJEKK_FJOR = MonthDay.of(5, 1);

    private SigrunRestClient client;
    private boolean isProd = Environment.current().isProd();


    SigrunConsumerImpl() {
        //CDI
    }

    @Inject
    public SigrunConsumerImpl(RestClient restClient) {
        this.client = new SigrunRestClient(restClient);

    }

    // Testformål
    SigrunConsumerImpl(SigrunRestClient restClient) {
        this.client = restClient;

    }

    @Override
    public SigrunResponse beregnetskatt(Long aktørId) {
         var årTilListeMedSkatt = ferdiglignedeBeregnetSkattÅr(aktørId).stream()
            .collect(Collectors.toMap(år -> år, år -> client.hentBeregnetSkattForAktørOgÅr(aktørId, år.toString())));

        return new SigrunResponse(årTilListeMedSkatt);
    }

    @Override
    public SigrunSummertSkattegrunnlagResponse summertSkattegrunnlag(Long aktørId) {
        Map<Year, Optional<SSGResponse>> summertskattegrunnlagMap = hentÅrsListeForSummertskattegrunnlag(aktørId).stream()
            .collect(Collectors.toMap(år -> år, år -> client.hentSummertskattegrunnlag(aktørId, år.toString())));
        return new SigrunSummertSkattegrunnlagResponse(summertskattegrunnlagMap);
    }

    private List<Year> ferdiglignedeBeregnetSkattÅr(Long aktørId) {
        Year iFjor = Year.now().minusYears(1L);
        if (iFjorErFerdiglignetBeregnet(aktørId, iFjor)) {
            return asList(iFjor, iFjor.minusYears(1L), iFjor.minusYears(2L));
        } else {
            Year iForifjor = iFjor.minusYears(1L);
            return asList(iForifjor, iForifjor.minusYears(1L), iForifjor.minusYears(2L));
        }
    }

    private List<Year> hentÅrsListeForSummertskattegrunnlag(Long aktørId) {
        Year iFjor = Year.now().minusYears(1L);
        //filteret(SummertSkattegrunnlagForeldrepenger) i Sigrun er ikke impl. tidligere enn 2018
        if (iFjor.equals(Year.of(2018))) {
            return List.of(iFjor);
        } else if (iFjor.equals(Year.of(2019))) {
            return List.of(iFjor, iFjor.minusYears(1L));
        }
        return ferdiglignedeBeregnetSkattÅr(aktørId);
    }

    private boolean iFjorErFerdiglignetBeregnet(Long aktørId, Year iFjor) {
        if (isProd && Year.now().minusYears(1).equals(iFjor) && MonthDay.now().isBefore(TIDLIGSTE_SJEKK_FJOR)) return false;
        return client.hentBeregnetSkattForAktørOgÅr(aktørId, iFjor.toString()).stream()
            .anyMatch(l -> l.tekniskNavn().equals(TEKNISK_NAVN));
    }



}
