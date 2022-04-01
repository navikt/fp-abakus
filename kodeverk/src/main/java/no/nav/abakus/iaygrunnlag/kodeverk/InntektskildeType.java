package no.nav.abakus.iaygrunnlag.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = Shape.OBJECT)
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

    public static final String KODEVERK = "INNTEKTS_KILDE";

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonIgnore
    private String navn;

    private String kode;

    private InntektskildeType(String kode) {
        this.kode = kode;
    }

    private InntektskildeType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static InntektskildeType fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(InntektskildeType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent InntektsKilde: " + kode);
        }
        return ad;
    }

    public static Map<String, InntektskildeType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @Override
    public String getNavn() {
        return navn;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }

}
