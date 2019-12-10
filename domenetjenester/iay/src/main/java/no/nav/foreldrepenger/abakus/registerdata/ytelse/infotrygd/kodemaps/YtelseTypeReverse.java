package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps;

import java.util.Map;

import org.slf4j.Logger;

import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.vedtak.domene.TemaUnderkategori;

public class YtelseTypeReverse {

    private static final Map<TemaUnderkategori, YtelseType> YTELSE_TYPE_MAP = Map.ofEntries(
        Map.entry(TemaUnderkategori.FORELDREPENGER_FODSEL, YtelseType.FORELDREPENGER),
        Map.entry(TemaUnderkategori.FORELDREPENGER_ADOPSJON, YtelseType.FORELDREPENGER),
        Map.entry(TemaUnderkategori.FORELDREPENGER_FODSEL_UTLAND, YtelseType.FORELDREPENGER),
        Map.entry(TemaUnderkategori.FORELDREPENGER_SVANGERSKAPSPENGER, YtelseType.SVANGERSKAPSPENGER),

        Map.entry(TemaUnderkategori.SYKEPENGER_SYKEPENGER, YtelseType.SYKEPENGER),
        Map.entry(TemaUnderkategori.SYKEPENGER_FORSIKRINGSRISIKO, YtelseType.SYKEPENGER),
        Map.entry(TemaUnderkategori.SYKEPENGER_REISETILSKUDD, YtelseType.SYKEPENGER),
        Map.entry(TemaUnderkategori.SYKEPENGER_UTENLANDSOPPHOLD, YtelseType.SYKEPENGER),

        Map.entry(TemaUnderkategori.PÅRØRENDE_OMSORGSPENGER, YtelseType.PÅRØRENDESYKDOM),
        Map.entry(TemaUnderkategori.PÅRØRENDE_OPPLÆRINGSPENGER, YtelseType.PÅRØRENDESYKDOM),
        Map.entry(TemaUnderkategori.PÅRØRENDE_PLEIETRENGENDE_SYKT_BARN, YtelseType.PÅRØRENDESYKDOM),
        Map.entry(TemaUnderkategori.PÅRØRENDE_PLEIETRENGENDE, YtelseType.PÅRØRENDESYKDOM),
        Map.entry(TemaUnderkategori.PÅRØRENDE_PLEIETRENGENDE_PÅRØRENDE, YtelseType.PÅRØRENDESYKDOM),
        Map.entry(TemaUnderkategori.PÅRØRENDE_PLEIEPENGER, YtelseType.PÅRØRENDESYKDOM)
    );


    public static YtelseType reverseMap(TemaUnderkategori tuk, Logger logger) {
        if (YTELSE_TYPE_MAP.get(tuk) == null) {
            logger.warn("Infotrygd ga ukjent kode for stønadskategori 2 {}", tuk.getKode());
        }
        return YTELSE_TYPE_MAP.getOrDefault(tuk, YtelseType.UDEFINERT);
    }
}
