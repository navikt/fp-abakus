package no.nav.abakus.iaygrunnlag.request;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.abakus.iaygrunnlag.kodeverk.Kodeverdi;
import no.nav.abakus.iaygrunnlag.kodeverk.TempAvledeKode;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum RegisterdataType implements Kodeverdi {

    ARBEIDSFORHOLD,
    LIGNET_NÆRING,
    INNTEKT_PENSJONSGIVENDE,
    INNTEKT_BEREGNINGSGRUNNLAG,
    INNTEKT_SAMMENLIGNINGSGRUNNLAG,
    YTELSE,
    ;

    private static final Map<String, RegisterdataType> KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "REGISTERDATA_TYPE";

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

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RegisterdataType fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(RegisterdataType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent RegisterdataType: " + kode);
        }
        return ad;
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
