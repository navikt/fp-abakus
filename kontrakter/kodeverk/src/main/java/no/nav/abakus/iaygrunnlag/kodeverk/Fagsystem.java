package no.nav.abakus.iaygrunnlag.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum Fagsystem implements Kodeverdi {

    FPSAK("FPSAK", "Vedtaksløsning Foreldrepenger", "FS36"),
    K9SAK("K9SAK", "Vedtaksløsning Folketrygdloven Kapittel 9", "K9SAK"),
    VLSP("VLSP", "Vedtaksløsning Sykepenger", "VLSP"),
    INFOTRYGD("INFOTRYGD", "Infotrygd", "IT01"),
    ARENA("ARENA", "Arena", "AO01"),
    KELVIN("KELVIN", "Vedtaksløsning Arbeidsavklaringspenger", "KELVIN"),
    DPSAK("DPSAK", "Vedtaksløsning Dagpenger", "DPSAK"),

    // Er ikke lagret i noe kildefelt
    AAREGISTERET("AAREGISTERET", "AAregisteret", "AR01"),

    ;

    private static final Map<String, Fagsystem> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private final String navn;
    private final String offisiellKode;
    @JsonValue
    private final String kode;

    Fagsystem(String kode, String navn, String offisiellKode) {
        this.kode = kode;
        this.navn = navn;
        this.offisiellKode = offisiellKode;
    }

    public static Fagsystem fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode)).orElseThrow(() -> new IllegalArgumentException("Ukjent Fagsystem: " + kode));
    }

    public static Map<String, Fagsystem> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public String getNavn() {
        return navn;
    }

    @Override
    public String getOffisiellKode() {
        return offisiellKode;
    }

    @Override
    public String getKode() {
        return kode;
    }
}
