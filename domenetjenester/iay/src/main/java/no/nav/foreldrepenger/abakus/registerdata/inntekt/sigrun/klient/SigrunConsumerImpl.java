package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.net.URI;
import java.time.MonthDay;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.abakus.iaygrunnlag.JsonObjectMapper;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SSGResponse;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SigrunSummertSkattegrunnlagResponse;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.exception.TekniskException;


@ApplicationScoped
public class SigrunConsumerImpl implements SigrunConsumer {

    private static final ObjectMapper mapper = JsonObjectMapper.getMapper();
    private static final String TEKNISK_NAVN = "skatteoppgjoersdato";

    private static final MonthDay TIDLIGSTE_SJEKK_FJOR = MonthDay.of(5, 1);

    private SigrunRestClient sigrunRestClient;
    private boolean isProd = Environment.current().isProd();


    SigrunConsumerImpl() {
        //CDI
    }

    @Inject
    public SigrunConsumerImpl(SigrunRestClient sigrunRestClient, @KonfigVerdi("SigrunRestBeregnetSkatt.url") URI endpoint) {
        this.sigrunRestClient = sigrunRestClient;
        this.sigrunRestClient.setEndpoint(endpoint);
    }

    private static <T> List<T> fromJsonList(String json, TypeReference<List<T>> typeReference) {
        try {
            return mapper.readValue(json, typeReference);
        } catch (IOException e) {
            throw ioExceptionVedLesing(e);
        }
    }

    private static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return mapper.readValue(json, typeReference);
        } catch (IOException e) {
            throw ioExceptionVedLesing(e);
        }
    }

    @Override
    public SigrunResponse beregnetskatt(Long aktørId) {
        Map<Year, List<BeregnetSkatt>> årTilListeMedSkatt = new HashMap<>();
        ferdiglignedeBeregnetSkattÅr(aktørId)
            .stream()
            .collect(Collectors.toMap(år -> år, år -> {
                String resultat = sigrunRestClient.hentBeregnetSkattForAktørOgÅr(aktørId, år.toString());
                return resultat != null ? resultat : "";
            }))
            .forEach((resulatÅr, skatt) -> leggTilBS(årTilListeMedSkatt, resulatÅr, skatt));

        return new SigrunResponse(årTilListeMedSkatt);
    }

    @Override
    public SigrunSummertSkattegrunnlagResponse summertSkattegrunnlag(Long aktørId) {
        Map<Year, Optional<SSGResponse>> summertskattegrunnlagMap = hentÅrsListeForSummertskattegrunnlag()
            .stream()
            .collect(Collectors.toMap(år -> år, år -> {
                String resultat = sigrunRestClient.hentSummertskattegrunnlag(aktørId, år.toString());
                if (resultat == null) {
                    return Optional.empty();
                }
                return Optional.of(fromJson(resultat, new TypeReference<>() {
                }));
            }));
        return new SigrunSummertSkattegrunnlagResponse(summertskattegrunnlagMap);
    }

    private void leggTilBS(Map<Year, List<BeregnetSkatt>> årTilListeMedSkatt, Year år, String skatt) {
        årTilListeMedSkatt.put(år, skatt.isEmpty()
            ? Collections.emptyList()
            : fromJsonList(skatt, new TypeReference<>() {
        }));
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

    private List<Year> hentÅrsListeForSummertskattegrunnlag() {
        Year iFjor = Year.now().minusYears(1L);
        //filteret(SummertSkattegrunnlagForeldrepenger) i Sigrun er ikke impl. tidligere enn 2018
        if (iFjor.equals(Year.of(2018))) {
            return List.of(iFjor);
        } else if (iFjor.equals(Year.of(2019))) {
            return List.of(iFjor, iFjor.minusYears(1L));
        }
        return asList(iFjor, iFjor.minusYears(1L), iFjor.minusYears(2L));
    }

    private boolean iFjorErFerdiglignetBeregnet(Long aktørId, Year iFjor) {
        if (isProd && Year.now().minusYears(1).equals(iFjor) && MonthDay.now().isBefore(TIDLIGSTE_SJEKK_FJOR)) return false;
        String json = sigrunRestClient.hentBeregnetSkattForAktørOgÅr(aktørId, iFjor.toString());
        List<BeregnetSkatt> beregnetSkatt = json != null
            ? fromJsonList(json, new TypeReference<>() {
        })
            : new ArrayList<>();
        return beregnetSkatt.stream()
            .anyMatch(l -> l.getTekniskNavn().equals(TEKNISK_NAVN));
    }

    private static TekniskException ioExceptionVedLesing(IOException cause) {
        return new TekniskException("F-918328", "Fikk IO exception ved parsing av JSON", cause);
    }

}
