package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.kodemaps;

import java.util.Map;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektPeriodeType;
import org.slf4j.Logger;

public class InntektPeriodeReverse {

    private static final String DAGLIG_KILDE = "D";
    private static final String UKENTLIG_KILDE = "U";
    private static final String BIUKENTLIG_KILDE = "F";
    private static final String MÅNEDLIG_KILDE = "M";
    private static final String ÅRLIG_KILDE = "Å";
    private static final String FASTSATT25PAVVIK_KILDE = "X";
    private static final String PREMIEGRUNNLAG_KILDE = "Y";

    private static final Map<String, InntektPeriodeType> INNTEKT_PERIODE_TYPE_MAP = Map.of(
            DAGLIG_KILDE,
            InntektPeriodeType.DAGLIG,
            UKENTLIG_KILDE,
            InntektPeriodeType.UKENTLIG,
            BIUKENTLIG_KILDE,
            InntektPeriodeType.BIUKENTLIG,
            MÅNEDLIG_KILDE,
            InntektPeriodeType.MÅNEDLIG,
            ÅRLIG_KILDE,
            InntektPeriodeType.ÅRLIG,
            FASTSATT25PAVVIK_KILDE,
            InntektPeriodeType.FASTSATT25PAVVIK,
            PREMIEGRUNNLAG_KILDE,
            InntektPeriodeType.PREMIEGRUNNLAG);

    public static InntektPeriodeType reverseMap(String kode, Logger logger) {
        if (INNTEKT_PERIODE_TYPE_MAP.get(kode) == null) {
            logger.warn("Infotrygd ga ukjent kode for inntektperiode {}", kode);
        }
        return INNTEKT_PERIODE_TYPE_MAP.getOrDefault(kode, InntektPeriodeType.UDEFINERT);
    }
}
