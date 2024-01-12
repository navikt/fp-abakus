package no.nav.abakus.iaygrunnlag.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum UtbetaltPensjonTrygdType implements UtbetaltYtelseType {

    KVALIFISERINGSSTØNAD("KVALIFISERINGSSTØNAD", "Kvalifiseringsstønad", "kvalifiseringstoenad"),
    UDEFINERT("-", "Undefined", null),
    ;

    public static final String KODEVERK = "PENSJON_TRYGD_BESKRIVELSE";
    private static final Map<String, UtbetaltPensjonTrygdType> KODER = new LinkedHashMap<>();

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

    private UtbetaltPensjonTrygdType(String kode) {
        this.kode = kode;
    }

    private UtbetaltPensjonTrygdType(String kode, String navn, String offisiellKode) {
        this.kode = kode;
        this.navn = navn;
        this.offisiellKode = offisiellKode;
    }

    public static UtbetaltPensjonTrygdType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode)).orElseThrow(() -> new IllegalArgumentException("Ukjent PensjonTrygdType: " + kode));
    }

    public static Map<String, UtbetaltPensjonTrygdType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public static UtbetaltPensjonTrygdType finnForKodeverkEiersKode(String offisiellDokumentType) {
        return Stream.of(values()).filter(k -> Objects.equals(k.offisiellKode, offisiellDokumentType)).findFirst().orElseThrow();
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

    @Override
    public boolean erUdefinert() {
        return UDEFINERT.equals(this);
    }
}
