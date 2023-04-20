package no.nav.abakus.iaygrunnlag.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonValue;

public enum LønnsinntektBeskrivelse implements Kodeverdi {
    KOMMUNAL_OMSORGSLOENN_OG_FOSTERHJEMSGODTGJOERELSE("KOMMUNAL_OMSORGSLOENN_OG_FOSTERHJEMSGODTGJOERELSE", "Kommunal omsorgslønn og fosterhjemsgodtgjørelse", "kommunalOmsorgsloennOgFosterhjemsgodtgjoerelse"),
    UDEFINERT("-", "Udefinert", null),
        ;
    public static final String KODEVERK = "LONNSINNTEKT_BESKRIVELSE";
    private static final Map<String, LønnsinntektBeskrivelse> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private String navn;
    private String offisiellKode;

    @JsonValue
    private String kode;

    private LønnsinntektBeskrivelse(String kode, String navn, String offisiellKode) {
        this.kode = kode;
        this.navn = navn;
        this.offisiellKode = offisiellKode;
    }

    public static LønnsinntektBeskrivelse fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode)).orElseThrow(() -> new IllegalArgumentException("Ukjent LønnsinntektBeskrivelse: " + kode));
    }

    public static Map<String, LønnsinntektBeskrivelse> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public static LønnsinntektBeskrivelse finnForKodeverkEiersKode(String offisiellDokumentType) {
        return List.of(values()).stream().filter(k -> Objects.equals(k.offisiellKode, offisiellDokumentType)).findFirst().orElse(UDEFINERT);
    }

    @Override
    public String getOffisiellKode() {
        return offisiellKode;
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
