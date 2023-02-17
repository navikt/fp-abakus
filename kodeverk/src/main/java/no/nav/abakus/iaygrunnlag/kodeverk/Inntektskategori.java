package no.nav.abakus.iaygrunnlag.kodeverk;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum Inntektskategori implements Kodeverdi {

    ARBEIDSTAKER("ARBEIDSTAKER", "Arbeidstaker"),
    FRILANSER("FRILANSER", "Frilans"),
    SELVSTENDIG_NÆRINGSDRIVENDE("SELVSTENDIG_NÆRINGSDRIVENDE", "Selvstendig næringsdrivende"),
    DAGPENGER("DAGPENGER", "Dagpenger"),
    ARBEIDSAVKLARINGSPENGER("ARBEIDSAVKLARINGSPENGER", "Arbeidsavklaringspenger"),
    SJØMANN("SJØMANN", "Arbeidstaker - Sjømann"),
    DAGMAMMA("DAGMAMMA", "Selvstendig næringsdrivende (dagmamma)"),
    JORDBRUKER("JORDBRUKER", "Selvstendig næringsdrivende - Jordbruker"),
    FISKER("FISKER", "Selvstendig næringsdrivende (fisker)"),
    ARBEIDSTAKER_UTEN_FERIEPENGER("ARBEIDSTAKER_UTEN_FERIEPENGER", "Arbeidstaker uten feriepenger"),
    UDEFINERT("-", "Ingen inntektskategori (default)"),
    ;

    public static final String KODEVERK = "INNTEKTSKATEGORI";
    private static final Map<String, Inntektskategori> KODER = new LinkedHashMap<>();

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

    Inntektskategori(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static Inntektskategori fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode)).orElseThrow(() -> new IllegalArgumentException("Ukjent Inntektskategori: " + kode));
    }

    public static Map<String, Inntektskategori> kodeMap() {
        return Collections.unmodifiableMap(KODER);
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

}
