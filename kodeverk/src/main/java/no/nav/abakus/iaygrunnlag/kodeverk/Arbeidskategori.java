package no.nav.abakus.iaygrunnlag.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum Arbeidskategori implements Kodeverdi {

    FISKER("FISKER", "Selvstendig næringsdrivende - Fisker"),
    ARBEIDSTAKER("ARBEIDSTAKER", "Arbeidstaker"),
    SELVSTENDIG_NÆRINGSDRIVENDE("SELVSTENDIG_NÆRINGSDRIVENDE", "Selvstendig næringsdrivende"),
    KOMBINASJON_ARBEIDSTAKER_OG_SELVSTENDIG_NÆRINGSDRIVENDE("KOMBINASJON_ARBEIDSTAKER_OG_SELVSTENDIG_NÆRINGSDRIVENDE",
        "Kombinasjon arbeidstaker og selvstendig næringsdrivende"),
    SJØMANN("SJØMANN", "Arbeidstaker - sjømann"),
    JORDBRUKER("JORDBRUKER", "Selvstendig næringsdrivende - Jordbruker"),
    DAGPENGER("DAGPENGER", "Tilstøtende ytelse - dagpenger"),
    INAKTIV("INAKTIV", "Inaktiv"),
    KOMBINASJON_ARBEIDSTAKER_OG_JORDBRUKER("KOMBINASJON_ARBEIDSTAKER_OG_JORDBRUKER",
        "Kombinasjon arbeidstaker og selvstendig næringsdrivende - jordbruker"),
    KOMBINASJON_ARBEIDSTAKER_OG_FISKER("KOMBINASJON_ARBEIDSTAKER_OG_FISKER", "Kombinasjon arbeidstaker og selvstendig næringsdrivende - fisker"),
    FRILANSER("FRILANSER", "Frilanser"),
    KOMBINASJON_ARBEIDSTAKER_OG_FRILANSER("KOMBINASJON_ARBEIDSTAKER_OG_FRILANSER", "Kombinasjon arbeidstaker og frilanser"),
    KOMBINASJON_ARBEIDSTAKER_OG_DAGPENGER("KOMBINASJON_ARBEIDSTAKER_OG_DAGPENGER", "Kombinasjon arbeidstaker og dagpenger"),
    DAGMAMMA("DAGMAMMA", "Selvstendig næringsdrivende - Dagmamma"),
    UGYLDIG("UGYLDIG", "Ugyldig"),
    UDEFINERT("-", "Ingen inntektskategori (default)"),
    ;

    public static final String KODEVERK = "ARBEIDSKATEGORI";
    private static final Map<String, Arbeidskategori> KODER = new LinkedHashMap<>();

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

    private Arbeidskategori(String kode) {
        this.kode = kode;
    }

    private Arbeidskategori(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static Arbeidskategori fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode)).orElseThrow(() -> new IllegalArgumentException("Ukjent Arbeidskategori: " + kode));
    }

    public static Map<String, Arbeidskategori> kodeMap() {
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
