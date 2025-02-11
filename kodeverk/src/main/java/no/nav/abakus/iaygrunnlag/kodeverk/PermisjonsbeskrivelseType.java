package no.nav.abakus.iaygrunnlag.kodeverk;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.*;
import java.util.stream.Stream;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum PermisjonsbeskrivelseType implements Kodeverdi {

    UDEFINERT("-", "Ikke definert", null),
    PERMISJON("PERMISJON", "Permisjon", "permisjon"),
    UTDANNINGSPERMISJON("UTDANNINGSPERMISJON", "Utdanningspermisjon", "utdanningspermisjon"), // Utgår 31/12-2022
    UTDANNINGSPERMISJON_IKKE_LOVFESTET("UTDANNINGSPERMISJON_IKKE_LOVFESTET", "Utdanningspermisjon (Ikke lovfestet)",
        "utdanningspermisjonIkkeLovfestet"),
    UTDANNINGSPERMISJON_LOVFESTET("UTDANNINGSPERMISJON_LOVFESTET", "Utdanningspermisjon (Lovfestet)", "utdanningspermisjonLovfestet"),
    VELFERDSPERMISJON("VELFERDSPERMISJON", "Velferdspermisjon", "velferdspermisjon"), // Utgår 31/12-2022
    ANNEN_PERMISJON_IKKE_LOVFESTET("ANNEN_PERMISJON_IKKE_LOVFESTET", "Andre ikke-lovfestede permisjoner", "andreIkkeLovfestedePermisjoner"),
    ANNEN_PERMISJON_LOVFESTET("ANNEN_PERMISJON_LOVFESTET", "Andre lovfestede permisjoner", "andreLovfestedePermisjoner"),
    PERMISJON_MED_FORELDREPENGER("PERMISJON_MED_FORELDREPENGER", "Permisjon med foreldrepenger", "permisjonMedForeldrepenger"),
    PERMITTERING("PERMITTERING", "Permittering", "permittering"),
    PERMISJON_VED_MILITÆRTJENESTE("PERMISJON_VED_MILITÆRTJENESTE", "Permisjon ved militærtjeneste", "permisjonVedMilitaertjeneste"),
    ;

    private static final Map<String, PermisjonsbeskrivelseType> KODER = new LinkedHashMap<>();

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

    private final String offisiellKode;

    PermisjonsbeskrivelseType(String kode, String navn, String offisiellKode) {
        this.kode = kode;
        this.navn = navn;
        this.offisiellKode = offisiellKode;
    }

    public static PermisjonsbeskrivelseType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode)).orElseThrow(() -> new IllegalArgumentException("Ukjent PermisjonsbeskrivelseType: " + kode));
    }

    public static Map<String, PermisjonsbeskrivelseType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public static PermisjonsbeskrivelseType finnForKodeverkEiersKode(String offisiellDokumentType) {
        return Stream.of(values()).filter(k -> Objects.equals(k.offisiellKode, offisiellDokumentType)).findFirst().orElse(UDEFINERT);
    }

    public String getNavn() {
        return navn;
    }

    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getOffisiellKode() {
        return offisiellKode;
    }


}
