package no.nav.abakus.iaygrunnlag.request;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import no.nav.abakus.iaygrunnlag.kodeverk.Kodeverdi;

import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = Shape.OBJECT)
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

    private String kode;

    private RegisterdataType() {
        this.kode = name();
    }

    @JsonCreator
    public static RegisterdataType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
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

    @Override
    public String getOffisiellKode() {
        return getKode();
    }

}
