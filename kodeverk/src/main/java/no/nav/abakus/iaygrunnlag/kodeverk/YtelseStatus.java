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
import com.fasterxml.jackson.annotation.JsonProperty.Access;

@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum YtelseStatus implements Kodeverdi {

    OPPRETTET("OPPR", "Opprettet"),
    UNDER_BEHANDLING("UBEH", "Under behandling"),
    LØPENDE("LOP", "Løpende"),
    AVSLUTTET("AVSLU", "Avsluttet"),
    
    UDEFINERT("-", "Ikke definert"),
    ;

    public static final YtelseStatus DEFAULT = OPPRETTET;
    private static final Map<String, YtelseStatus> KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "YTELSE_STATUS";

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

    private YtelseStatus(String kode) {
        this.kode = kode;
    }

    private YtelseStatus(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator
    public static YtelseStatus fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent YtelseStatus: " + kode);
        }
        return ad;
    }

    public static Map<String, YtelseStatus> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @Override
    public String getNavn() {
        return navn;
    }

    @JsonProperty(value="kode")
    @Override
    public String getKode() {
        return kode;
    }

    @JsonProperty(value="kodeverk", access = Access.READ_ONLY)
    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

}
