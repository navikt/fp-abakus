package no.nav.abakus.iaygrunnlag.kodeverk;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@JsonAutoDetect(
        getterVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE,
        fieldVisibility = Visibility.ANY)
public enum InntektsmeldingInnsendingsårsakType implements Kodeverdi {
    NY("NY", "NY"),
    ENDRING("ENDRING", "ENDRING"),
    UDEFINERT("-", "UDEFINERT"),
    ;

    private static final Map<String, InntektsmeldingInnsendingsårsakType> KODER = new LinkedHashMap<>();

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

    InntektsmeldingInnsendingsårsakType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static InntektsmeldingInnsendingsårsakType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode))
                .orElseThrow(() -> new IllegalArgumentException("Ukjent InntektsmeldingInnsendingsårsak: " + kode));
    }

    public static Map<String, InntektsmeldingInnsendingsårsakType> kodeMap() {
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
