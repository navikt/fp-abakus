package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps;

import java.util.Map;

import org.slf4j.Logger;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;

public class TemaReverse {

    private static final Map<String, YtelseType> TEMA_MAP = Map.ofEntries(Map.entry("FA", YtelseType.FORELDREPENGER),
        Map.entry("BS", YtelseType.OMSORGSPENGER), Map.entry("SP", YtelseType.SYKEPENGER));


    public static YtelseType reverseMap(String kode, Logger logger) {
        if (TEMA_MAP.get(kode) == null) {
            logger.warn("Infotrygd ga ukjent kode for stønadskategori 1 {}", kode);
        }
        return TEMA_MAP.getOrDefault(kode, YtelseType.UDEFINERT);
    }
}
