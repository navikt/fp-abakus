package no.nav.abakus.iaygrunnlag.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum YtelseType implements Kodeverdi {

    /**
     * Folketrygdloven K4 ytelser.
     */
    DAGPENGER("DAG", "Dagpenger"),

    /**
     * Ny ytelse for kompenasasjon for koronatiltak for Selvstendig næringsdrivende og Frilansere (Anmodning 10).
     */
    FRISINN("FRISINN", "FRISINN"),

    /**
     * Folketrygdloven K8 ytelser.
     */
    SYKEPENGER("SP", "Sykepenger"),

    /**
     * Folketrygdloven K9 ytelser.
     */
    PLEIEPENGER_SYKT_BARN("PSB", "Pleiepenger sykt barn"),
    PLEIEPENGER_NÆRSTÅENDE("PPN", "Pleiepenger nærstående"),
    OMSORGSPENGER("OMP", "Omsorgspenger"),
    OPPLÆRINGSPENGER("OLP", "Opplæringspenger"),

    /**
     * Folketrygdloven K11 ytelser.
     */
    ARBEIDSAVKLARINGSPENGER("AAP", "Arbeidsavklaringspenger"),

    /**
     * Folketrygdloven K14 ytelser.
     */
    ENGANGSTØNAD("ES", "Engangsstønad"),
    FORELDREPENGER("FP", "Foreldrepenger"),
    SVANGERSKAPSPENGER("SVP", "Svangerskapspenger"),

    /**
     * Folketrygdloven K15 ytelser.
     */
    ENSLIG_FORSØRGER("EF", "Enslig forsørger"),

    UDEFINERT("-", "Ikke definert"),
    ;

    private static final Map<String, YtelseType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private final String navn;

    @JsonValue
    private final String kode;

    YtelseType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static YtelseType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode)).orElseThrow(() -> new IllegalArgumentException("Ukjent YtelseType: " + kode));
    }

    public static Map<String, YtelseType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public static List<YtelseType> abakusYtelser() {
        return List.of(YtelseType.FORELDREPENGER, YtelseType.SVANGERSKAPSPENGER);
    }

    @Override
    public String getKode() {
        return kode;
    }

    public String getNavn() {
        return navn;
    }

}
