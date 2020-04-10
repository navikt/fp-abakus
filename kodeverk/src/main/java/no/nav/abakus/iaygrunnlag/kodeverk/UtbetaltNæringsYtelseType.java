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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum UtbetaltNæringsYtelseType implements UtbetaltYtelseType {

    VEDERLAG_DAGMAMMA_I_EGETHJEM("VEDERLAG_DAGMAMMA_I_EGETHJEM", "Vederlag dagmamma i egethjem", "vederlagDagmammaIEgetHjem"),
    VEDERLAG("VEDERLAG", "Vederlag", "vederlag"),
    SYKEPENGER_TIL_JORD_OG_SKOGBRUKERE("SYKEPENGER_TIL_JORD_OG_SKOGBRUKERE", "Sykepenger til jord og skogbrukere", "sykepengerTilJordOgSkogbrukere"),
    SYKEPENGER_TIL_FISKER("SYKEPENGER_TIL_FISKER", "Sykepenger til fisker", "sykepengerTilFisker"),
    SYKEPENGER_TIL_DAGMAMMA("SYKEPENGER_TIL_DAGMAMMA", "Sykepenger til dagmamma", "sykepengerTilDagmamma"),
    SYKEPENGER("SYKEPENGER", "Sykepenger (næringsinntekt)", "sykepenger"),
    LOTT_KUN_TRYGDEAVGIFT("LOTT_KUN_TRYGDEAVGIFT", "Lott kun trygdeavgift", "lottKunTrygdeavgift"),
    DAGPENGER_VED_ARBEIDSLØSHET("DAGPENGER_VED_ARBEIDSLØSHET", "Dagpenger ved arbeidsløshet", "dagpengerVedArbeidsloeshet"),
    DAGPENGER_TIL_FISKER("DAGPENGER_TIL_FISKER", "Dagpenger til fisker", "dagpengerTilFisker"),
    ANNET("ANNET", "Annet", "annet"),
    UDEFINERT("-", "Udefinert", null),
    ;

    private static final Map<String, UtbetaltNæringsYtelseType> KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "NÆRINGSINNTEKT_TYPE";

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonIgnore
    private String navn;

    @JsonIgnore
    private String offisiellKode;

    private String kode;

    private UtbetaltNæringsYtelseType(String kode, String navn, String offisiellKode) {
        this.kode = kode;
        this.navn = navn;
        this.offisiellKode = offisiellKode;
    }

    @JsonCreator
    public static UtbetaltNæringsYtelseType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent NæringsinntektType: " + kode);
        }
        return ad;
    }

    public static Map<String, UtbetaltNæringsYtelseType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @Override
    public String getOffisiellKode() {
        return offisiellKode;
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

    @JsonProperty(value = "kode")
    @Override
    public String getKode() {
        return kode;
    }

    public static UtbetaltNæringsYtelseType finnForKodeverkEiersKode(String offisiellDokumentType) {
        return List.of(values()).stream().filter(k -> Objects.equals(k.offisiellKode, offisiellDokumentType)).findFirst().orElse(UDEFINERT);
    }

}