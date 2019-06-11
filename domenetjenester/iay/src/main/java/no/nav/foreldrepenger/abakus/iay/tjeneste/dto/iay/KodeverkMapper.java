package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.Map;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.abakus.domene.iay.NæringsinntektType;
import no.nav.foreldrepenger.abakus.domene.iay.OffentligYtelseType;
import no.nav.foreldrepenger.abakus.domene.iay.PensjonTrygdType;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.Kodeverk;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtbetaltNæringsYtelseType;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtbetaltPensjonTrygdType;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtbetaltYtelseFraOffentligeType;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.UtbetaltYtelseType;

final class KodeverkMapper {
    private static final Map<String, String> YTELSETYPE_FPSAK_TIL_ABAKUS;
    private static final Map<String, String> YTELSETYPE_ABAKUS_TIL_FPSAK;

    private KodeverkMapper() {
    }

    static {
        YTELSETYPE_FPSAK_TIL_ABAKUS = Map.of(
            "FORELDREPENGER", "FP",
            "ENGANGSSTØNAD", "ES",
            "SVANGERSKAPSPENGER", "SVP");
        
        YTELSETYPE_ABAKUS_TIL_FPSAK = YTELSETYPE_FPSAK_TIL_ABAKUS.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    static String getFpsakYtelseTypeFraAbakus(String kode) {
        return YTELSETYPE_ABAKUS_TIL_FPSAK.get(kode);
    }

    static String getAbakusYtelseTypeFraFpsak(String kode) {
        return YTELSETYPE_FPSAK_TIL_ABAKUS.get(kode);
    }

    static UtbetaltYtelseType mapYtelseTypeTilDto(no.nav.foreldrepenger.abakus.domene.iay.YtelseType ytelseType) {
        if (ytelseType == null || ytelseType.getKode().equals("-")) {
            return null;
        }
        switch (ytelseType.getKodeverk()) {
            case UtbetaltYtelseFraOffentligeType.KODEVERK:
                return new UtbetaltYtelseFraOffentligeType(ytelseType.getKode());
            case UtbetaltNæringsYtelseType.KODEVERK:
                return new UtbetaltNæringsYtelseType(ytelseType.getKode());
            case UtbetaltPensjonTrygdType.KODEVERK:
                return new UtbetaltPensjonTrygdType(ytelseType.getKode());
            default:
                throw new IllegalArgumentException("Ukjent YtelseType: " + ytelseType + ", kan ikke mappes til " + UtbetaltYtelseType.class.getName());
        }

    }


    public static no.nav.foreldrepenger.abakus.domene.iay.YtelseType mapUtbetaltYtelseTypeTilGrunnlag(UtbetaltYtelseType type) {
        Kodeverk kodeverk = (Kodeverk) type;
        if(kodeverk==null || kodeverk.getKode().equals("-")) return null;
        
        String kode = kodeverk.getKode();
        switch (kodeverk.getKodeverk()) {
            case UtbetaltYtelseFraOffentligeType.KODEVERK:
                return new OffentligYtelseType(kode);
            case UtbetaltNæringsYtelseType.KODEVERK:
                return new NæringsinntektType(kode);
            case UtbetaltPensjonTrygdType.KODEVERK:
                return new PensjonTrygdType(kode);
            default:
                throw new IllegalArgumentException("Ukjent UtbetaltYtelseType: " + type);
        }
    }
}
