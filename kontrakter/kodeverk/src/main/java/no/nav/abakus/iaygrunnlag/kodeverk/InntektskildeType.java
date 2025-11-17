package no.nav.abakus.iaygrunnlag.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum InntektskildeType implements Kodeverdi {

    UDEFINERT("-", "Ikke definert"),
    INNTEKT_OPPTJENING("INNTEKT_OPPTJENING", "INNTEKT_OPPTJENING"),
    INNTEKT_BEREGNING("INNTEKT_BEREGNING", "INNTEKT_BEREGNING"),
    INNTEKT_SAMMENLIGNING("INNTEKT_SAMMENLIGNING", "INNTEKT_SAMMENLIGNING"),
    SIGRUN("SIGRUN", "Sigrun"),
    VANLIG("VANLIG", "Vanlig"),
    ;

    private static final Map<String, InntektskildeType> KODER = new LinkedHashMap<>();

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

    InntektskildeType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static InntektskildeType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode)).orElseThrow(() -> new IllegalArgumentException("Ukjent InntektsKilde: " + kode));
    }

    public static Map<String, InntektskildeType> kodeMap() {
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
