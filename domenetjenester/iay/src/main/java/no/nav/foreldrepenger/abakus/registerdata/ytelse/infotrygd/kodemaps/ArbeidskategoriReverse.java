package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps;

import java.util.Map;

import org.slf4j.Logger;

import no.nav.abakus.iaygrunnlag.kodeverk.Arbeidskategori;

public class ArbeidskategoriReverse  {

    private static final Map<String, Arbeidskategori> ARBEIDSKATEGORI_MAP = Map.ofEntries(
        Map.entry("00", Arbeidskategori.FISKER),
        Map.entry("01", Arbeidskategori.ARBEIDSTAKER),
        Map.entry("02", Arbeidskategori.SELVSTENDIG_NÆRINGSDRIVENDE),
        Map.entry("03", Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_SELVSTENDIG_NÆRINGSDRIVENDE),
        Map.entry("04", Arbeidskategori.SJØMANN),
        Map.entry("05", Arbeidskategori.JORDBRUKER),
        Map.entry("06", Arbeidskategori.DAGPENGER),
        Map.entry("07", Arbeidskategori.INAKTIV),
        Map.entry("08", Arbeidskategori.ARBEIDSTAKER),
        Map.entry("09", Arbeidskategori.ARBEIDSTAKER),
        Map.entry("10", Arbeidskategori.SJØMANN),
        Map.entry("11", Arbeidskategori.SJØMANN),
        Map.entry("12", Arbeidskategori.ARBEIDSTAKER),
        Map.entry("13", Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_JORDBRUKER),
        Map.entry("14", Arbeidskategori.UGYLDIG),
        Map.entry("15", Arbeidskategori.SELVSTENDIG_NÆRINGSDRIVENDE),
        Map.entry("16", Arbeidskategori.SELVSTENDIG_NÆRINGSDRIVENDE),
        Map.entry("17", Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_FISKER),
        Map.entry("18", Arbeidskategori.UGYLDIG),
        Map.entry("19", Arbeidskategori.FRILANSER),
        Map.entry("20", Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_FRILANSER),
        Map.entry("21", Arbeidskategori.ARBEIDSTAKER),
        Map.entry("22", Arbeidskategori.SJØMANN),
        Map.entry("23", Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_DAGPENGER),
        Map.entry("24", Arbeidskategori.FRILANSER),
        Map.entry("25", Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_FRILANSER),
        Map.entry("26", Arbeidskategori.DAGMAMMA),
        Map.entry("27", Arbeidskategori.ARBEIDSTAKER),
        Map.entry("30", Arbeidskategori.UGYLDIG),
        Map.entry("99", Arbeidskategori.UGYLDIG));


    public static Arbeidskategori reverseMap(String kode, Logger logger) {
        if (ARBEIDSKATEGORI_MAP.get(kode) == null) {
            logger.warn("Infotrygd ga ukjent kode for arbeidskategori {}", kode);
        }
        return ARBEIDSKATEGORI_MAP.getOrDefault(kode, Arbeidskategori.UDEFINERT);
    }
}
