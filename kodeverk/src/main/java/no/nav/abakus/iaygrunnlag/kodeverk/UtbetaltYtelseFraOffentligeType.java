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
public enum UtbetaltYtelseFraOffentligeType implements UtbetaltYtelseType {

    AAP("AAP", "Arbeidsavklaringspenger", "arbeidsavklaringspenger"),
    @Deprecated(forRemoval = true) // På vei til DAGPENGER
    DAGPENGER_FISKER("DAGPENGER_FISKER", "Dagpenger til fisker som bare har hyre", "dagpengerTilFiskerSomBareHarHyre"),
    @Deprecated(forRemoval = true) // På vei til DAGPENGER
    DAGPENGER_ARBEIDSLØS("DAGPENGER_ARBEIDSLØS", "Dagpenger ved arbeidsløshet", "dagpengerVedArbeidsloeshet"),
    DAGPENGER("DAGPENGER", "Dagpenger arbeid og hyre", null),
    FORELDREPENGER("FORELDREPENGER", "Foreldrepenger", "foreldrepenger"),
    OVERGANGSSTØNAD_ENSLIG("OVERGANGSSTØNAD_ENSLIG", "Overgangsstønad til enslig mor eller far",
        "overgangsstoenadTilEnsligMorEllerFarSomBegynteAaLoepe1April2014EllerSenere"),
    SVANGERSKAPSPENGER("SVANGERSKAPSPENGER", "Svangerskapspenger", "svangerskapspenger"),
    SYKEPENGER("SYKEPENGER", "Sykepenger", "sykepenger"),
    @Deprecated(forRemoval = true) // På vei til SYKEPENGER
    SYKEPENGER_FISKER("SYKEPENGER_FISKER", "Sykepenger fisker", "sykepengerTilFiskerSomBareHarHyre"),
    VENTELØNN("VENTELØNN", "Ventelønn", "venteloenn"),

    UDEFINERT("-", "UNDEFINED", null),
    ;

    public static final String KODEVERK = "YTELSE_FRA_OFFENTLIGE";
    private static final Map<String, UtbetaltYtelseFraOffentligeType> KODER = new LinkedHashMap<>();

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

    private UtbetaltYtelseFraOffentligeType(String kode) {
        this.kode = kode;
    }

    private UtbetaltYtelseFraOffentligeType(String kode, String navn, String offisiellKode) {
        this.kode = kode;
        this.navn = navn;
        this.offisiellKode = offisiellKode;
    }

    public static UtbetaltYtelseFraOffentligeType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode)).orElseThrow(() -> new IllegalArgumentException("Ukjent OffentligYtelseType: " + kode));
    }

    public static Map<String, UtbetaltYtelseFraOffentligeType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public static UtbetaltYtelseFraOffentligeType finnForKodeverkEiersKode(String offisiellDokumentType) {
        return List.of(values()).stream().filter(k -> Objects.equals(k.offisiellKode, offisiellDokumentType)).findFirst().orElse(UDEFINERT);
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
