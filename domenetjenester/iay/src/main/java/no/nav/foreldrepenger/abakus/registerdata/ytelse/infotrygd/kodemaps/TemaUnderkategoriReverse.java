package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps;

import java.util.Map;

import org.slf4j.Logger;

import no.nav.foreldrepenger.abakus.kodeverk.TemaUnderkategori;
import org.slf4j.LoggerFactory;

public class TemaUnderkategoriReverse {

    private static final Logger LOG = LoggerFactory.getLogger(TemaUnderkategoriReverse.class);

    private static final Map<String, TemaUnderkategori> BEHANDLING_TEMA_MAP = Map.ofEntries(
        Map.entry(TemaUnderkategori.FORELDREPENGER_FODSEL.getKode(), TemaUnderkategori.FORELDREPENGER_FODSEL),
        Map.entry("FP", TemaUnderkategori.FORELDREPENGER_FODSEL),
        Map.entry(TemaUnderkategori.FORELDREPENGER_ADOPSJON.getKode(), TemaUnderkategori.FORELDREPENGER_ADOPSJON),
        Map.entry(TemaUnderkategori.FORELDREPENGER_FODSEL_UTLAND.getKode(), TemaUnderkategori.FORELDREPENGER_FODSEL_UTLAND),
        Map.entry(TemaUnderkategori.FORELDREPENGER_SVANGERSKAPSPENGER.getKode(), TemaUnderkategori.FORELDREPENGER_SVANGERSKAPSPENGER),
        Map.entry(TemaUnderkategori.ENGANGSSTONAD_ADOPSJON.getKode(), TemaUnderkategori.ENGANGSSTONAD_ADOPSJON),
        Map.entry(TemaUnderkategori.ENGANGSSTONAD_FODSEL.getKode(), TemaUnderkategori.ENGANGSSTONAD_FODSEL),

        Map.entry(TemaUnderkategori.SYKEPENGER_SYKEPENGER.getKode(), TemaUnderkategori.SYKEPENGER_SYKEPENGER),
        Map.entry(TemaUnderkategori.SYKEPENGER_FORSIKRINGSRISIKO.getKode(), TemaUnderkategori.SYKEPENGER_FORSIKRINGSRISIKO),
        Map.entry(TemaUnderkategori.SYKEPENGER_REISETILSKUDD.getKode(), TemaUnderkategori.SYKEPENGER_REISETILSKUDD),
        Map.entry(TemaUnderkategori.SYKEPENGER_UTENLANDSOPPHOLD.getKode(), TemaUnderkategori.SYKEPENGER_UTENLANDSOPPHOLD),

        Map.entry(TemaUnderkategori.PÅRØRENDE_OMSORGSPENGER.getKode(), TemaUnderkategori.PÅRØRENDE_OMSORGSPENGER),
        Map.entry(TemaUnderkategori.PÅRØRENDE_OPPLÆRINGSPENGER.getKode(), TemaUnderkategori.PÅRØRENDE_OPPLÆRINGSPENGER),
        Map.entry(TemaUnderkategori.PÅRØRENDE_PLEIETRENGENDE_SYKT_BARN.getKode(), TemaUnderkategori.PÅRØRENDE_PLEIETRENGENDE_SYKT_BARN),
        Map.entry(TemaUnderkategori.PÅRØRENDE_PLEIETRENGENDE.getKode(), TemaUnderkategori.PÅRØRENDE_PLEIETRENGENDE),
        Map.entry(TemaUnderkategori.PÅRØRENDE_PLEIETRENGENDE_PÅRØRENDE.getKode(), TemaUnderkategori.PÅRØRENDE_PLEIETRENGENDE_PÅRØRENDE),
        Map.entry(TemaUnderkategori.PÅRØRENDE_PLEIEPENGER.getKode(), TemaUnderkategori.PÅRØRENDE_PLEIEPENGER),

        Map.entry(TemaUnderkategori.OVERGANGSSTØNAD.getKode(), TemaUnderkategori.OVERGANGSSTØNAD)
    );


    public static TemaUnderkategori reverseMap(String kode) {
        if (!BEHANDLING_TEMA_MAP.containsKey(kode)) {
            LOG.warn("Infotrygd ga ukjent kode for stønadskategori 2 {}", kode);
        }
        return BEHANDLING_TEMA_MAP.getOrDefault(kode, TemaUnderkategori.UDEFINERT);
    }
}
