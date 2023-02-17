package no.nav.abakus.iaygrunnlag.request;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.abakus.iaygrunnlag.kodeverk.Kodeverdi;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum RegisterdataType implements Kodeverdi {

    ARBEIDSFORHOLD,
    LIGNET_NÆRING,
    INNTEKT_PENSJONSGIVENDE,
    INNTEKT_BEREGNINGSGRUNNLAG,
    INNTEKT_SAMMENLIGNINGSGRUNNLAG,
    YTELSE,
    ;

    public static final String KODEVERK = "REGISTERDATA_TYPE";
    private static final Map<String, RegisterdataType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private String kode;

    private RegisterdataType() {
        this.kode = name();
    }

    public static RegisterdataType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode)).orElseThrow(() -> new IllegalArgumentException("Ukjent RegisterdataType: " + kode));
    }

    public static Map<String, RegisterdataType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @Override
    public String getNavn() {
        return kode;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @JsonProperty(value = "kode")
    @Override
    public String getKode() {
        return kode;
    }

}
