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
    private static final Map<String, LønnsinntektBeskrivelse> KODER = new LinkedHashMap<>();

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

    LønnsinntektBeskrivelse(String kode, String navn, String offisiellKode) {
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

    public String getNavn() {
        return navn;
    }

    @Override
    public String getKode() {
        return kode;
    }

}
