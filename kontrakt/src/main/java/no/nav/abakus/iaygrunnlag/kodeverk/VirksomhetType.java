package no.nav.abakus.iaygrunnlag.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum VirksomhetType implements Kodeverdi {

    DAGMAMMA("DAGMAMMA", "Dagmamma i eget hjem/familiebarnehage"),
    ENKELTPERSONFORETAK("ENK", "Enkeltpersonforetak"),
    FISKE("FISKE", "Fiske"),
    FRILANSER("FRILANSER", "Frilanser"),
    JORDBRUK_SKOGBRUK("JORDBRUK_SKOGBRUK", "Jordbruk"),
    ANNEN("ANNEN", "Annen næringsvirksomhet"),
    UDEFINERT("-", "Ikke definert"),
    ;

    private static final Map<String, VirksomhetType> KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "VIRKSOMHET_TYPE";

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonIgnore
    private String navn;

    @JsonProperty(value = "kode")
    private String kode;

    VirksomhetType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    @JsonCreator
    public static VirksomhetType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent VirksomhetType: " + kode);
        }
        return ad;
    }

    public static Map<String, VirksomhetType> kodeMap() {
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

    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getOffisiellKode() {
        return getKode();
    }

}