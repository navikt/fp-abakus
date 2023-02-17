package no.nav.abakus.iaygrunnlag.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum InntektspostType implements Kodeverdi {

    UDEFINERT("-", "Ikke definert", null),
    LØNN("LØNN", "Lønn", "LONN"),
    YTELSE("YTELSE", "Ytelse", "YTELSE"),
    SELVSTENDIG_NÆRINGSDRIVENDE("SELVSTENDIG_NÆRINGSDRIVENDE", "Selvstendig næringsdrivende", "-"),
    NÆRING_FISKE_FANGST_FAMBARNEHAGE("NÆRING_FISKE_FANGST_FAMBARNEHAGE", "Jordbruk/Skogbruk/Fiske/FamilieBarnehage",
        "personinntektFiskeFangstFamilebarnehage"),
    ;

    public static final String KODEVERK = "INNTEKTSPOST_TYPE";
    private static final Map<String, InntektspostType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private String navn;

    @JsonValue
    private String kode;

    private String offisiellKode;

    private InntektspostType(String kode) {
        this.kode = kode;
    }

    private InntektspostType(String kode, String navn, String offisiellKode) {
        this.kode = kode;
        this.navn = navn;
        this.offisiellKode = offisiellKode;
    }

    public static InntektspostType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode)).orElseThrow(() -> new IllegalArgumentException("Ukjent InntektspostType: " + kode));
    }

    public static Map<String, InntektspostType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @Override
    public String getNavn() {
        return navn;
    }

    @Override
    public String getKodeverk() {
        return KODEVERK;
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
