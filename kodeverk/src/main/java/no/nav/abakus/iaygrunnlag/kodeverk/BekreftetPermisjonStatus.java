package no.nav.abakus.iaygrunnlag.kodeverk;

/**
 * <p>
 * Definerer statuser for bekreftet permisjoner
 * </p>
 */

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum BekreftetPermisjonStatus implements Kodeverdi {

    UDEFINERT("-", "UDEFINERT"),
    BRUK_PERMISJON("BRUK_PERMISJON", "Bruk permisjonen til arbeidsforholdet"),
    IKKE_BRUK_PERMISJON("IKKE_BRUK_PERMISJON", "Ikke bruk permisjonen til arbeidsforholdet"),
    UGYLDIGE_PERIODER("UGYLDIGE_PERIODER", "Arbeidsforholdet inneholder permisjoner med ugyldige perioder"),
    ;

    private static final Map<String, BekreftetPermisjonStatus> KODER = new LinkedHashMap<>();

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

    BekreftetPermisjonStatus(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static BekreftetPermisjonStatus fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode)).orElseThrow(() -> new IllegalArgumentException("Ukjent BekreftetPermisjonStatus: " + kode));
    }

    public static Map<String, BekreftetPermisjonStatus> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public String getNavn() {
        return navn;
    }

    @Override
    public String getKode() {
        return kode;
    }

}
