/**
 * Næringsinntekter rapportert av NAV.
 * <p>
 * Eks, sykepenger ved sykepengerforsikring
 */
package no.nav.abakus.iaygrunnlag.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum UtbetaltNæringsYtelseType implements UtbetaltYtelseType {

    @Deprecated(forRemoval = true) // Bruk VEDERLAG
    VEDERLAG_DAGMAMMA_I_EGETHJEM("VEDERLAG_DAGMAMMA_I_EGETHJEM", "Vederlag dagmamma i egethjem", List.of()),
    VEDERLAG("VEDERLAG", "Vederlag", List.of("vederlag", "vederlagDagmammaIEgetHjem")),
    @Deprecated(forRemoval = true) // Bruk SYKEPENGER_NÆRING
    SYKEPENGER_TIL_JORD_OG_SKOGBRUKERE("SYKEPENGER_TIL_JORD_OG_SKOGBRUKERE", "Sykepenger til jord og skogbrukere", List.of()),
    @Deprecated(forRemoval = true) // Bruk SYKEPENGER_NÆRING
    SYKEPENGER_TIL_FISKER("SYKEPENGER_TIL_FISKER", "Sykepenger til fisker", List.of()),
    @Deprecated(forRemoval = true) // Bruk SYKEPENGER_NÆRING
    SYKEPENGER_TIL_DAGMAMMA("SYKEPENGER_TIL_DAGMAMMA", "Sykepenger til dagmamma", List.of()),
    @Deprecated(forRemoval = true) // Bruk SYKEPENGER_NÆRING
    SYKEPENGER("SYKEPENGER", "Sykepenger (næringsinntekt)", List.of()),
    LOTT_KUN_TRYGDEAVGIFT("LOTT_KUN_TRYGDEAVGIFT", "Lott kun trygdeavgift", "lottKunTrygdeavgift"),
    @Deprecated(forRemoval = true) // Bruk DAGPENGER_NÆRING
    DAGPENGER_VED_ARBEIDSLØSHET("DAGPENGER_VED_ARBEIDSLØSHET", "Dagpenger ved arbeidsløshet", List.of()),
    @Deprecated(forRemoval = true) // Bruk DAGPENGER_NÆRING
    DAGPENGER_TIL_FISKER("DAGPENGER_TIL_FISKER", "Dagpenger til fisker", List.of()),
    ANNET("ANNET", "Annet", "annet"),
    KOMPENSASJON_FOR_TAPT_PERSONINNTEKT("KOMPENSASJON_FOR_TAPT_PERSONINNTEKT", "Kompensasjon for tapt personinntekt",
        "kompensasjonForTaptPersoninntekt"),

    SYKEPENGER_NÆRING("SYKEPENGER_NÆRING", "Sykepenger næring", List.of("sykepengerTilJordOgSkogbrukere", "sykepengerTilFisker", "sykepengerTilDagmamma", "sykepenger")),
    DAGPENGER_NÆRING("DAGPENGER_NÆRING", "Dagpenger næring", List.of("dagpengerVedArbeidsloeshet", "dagpengerTilFisker")),

    UDEFINERT("-", "Udefinert", List.of()),
    ;

    public static final String KODEVERK = "NÆRINGSINNTEKT_TYPE";
    private static final Map<String, UtbetaltNæringsYtelseType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private String navn;
    private List<String> offisiellKode;

    @JsonValue
    private String kode;

    private UtbetaltNæringsYtelseType(String kode, String navn, String offisiellKode) {
        this(kode, navn, List.of(offisiellKode));
    }

    private UtbetaltNæringsYtelseType(String kode, String navn, List<String> offisiellKode) {
        this.kode = kode;
        this.navn = navn;
        this.offisiellKode = offisiellKode;
    }

    public static UtbetaltNæringsYtelseType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode)).orElseThrow(() -> new IllegalArgumentException("Ukjent NæringsinntektType: " + kode));
    }

    public static Map<String, UtbetaltNæringsYtelseType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public static UtbetaltNæringsYtelseType finnForKodeverkEiersKode(String offisiellDokumentType) {
        return List.of(values()).stream().filter(k -> k.offisiellKode.contains(offisiellDokumentType)).findFirst().orElse(UDEFINERT);
    }

    @Override
    public String getOffisiellKode() {
        return offisiellKode.stream().findFirst().orElse(null);
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
    public boolean erUdefinert() {
        return UDEFINERT.equals(this);
    }
}
