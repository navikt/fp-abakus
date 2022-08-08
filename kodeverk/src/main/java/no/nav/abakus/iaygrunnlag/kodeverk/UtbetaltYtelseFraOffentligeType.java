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
public enum UtbetaltYtelseFraOffentligeType implements UtbetaltYtelseType {

    AAP("AAP", "Arbeidsavklaringspenger", "arbeidsavklaringspenger"),
    DAGPENGER_FISKER("DAGPENGER_FISKER", "Dagpenger til fisker som bare har hyre", "dagpengerTilFiskerSomBareHarHyre"),
    DAGPENGER_ARBEIDSLØS("DAGPENGER_ARBEIDSLØS", "Dagpenger ved arbeidsløshet", "dagpengerVedArbeidsloeshet"),
    FORELDREPENGER("FORELDREPENGER", "Foreldrepenger", "foreldrepenger"),
    OVERGANGSSTØNAD_ENSLIG("OVERGANGSSTØNAD_ENSLIG", "Overgangsstønad til enslig mor eller far", "overgangsstoenadTilEnsligMorEllerFarSomBegynteAaLoepe1April2014EllerSenere"),
    SVANGERSKAPSPENGER("SVANGERSKAPSPENGER", "Svangerskapspenger", "svangerskapspenger"),
    SYKEPENGER("SYKEPENGER", "Sykepenger", "sykepenger"),
    SYKEPENGER_FISKER("SYKEPENGER_FISKER", "Sykepenger fisker", "sykepengerTilFiskerSomBareHarHyre"),
    UFØRETRYGD("UFØRETRYGD", "Uføretrygd", "ufoeretrygd"),
    UFØRETRYGD_ETTEROPPGJØR("UFØRETRYGD_ETTEROPPGJØR", "Uføretrygd etteroppgjør", "ufoereytelseEtteroppgjoer"),
    UNDERHOLDNINGSBIDRAG_BARN("UNDERHOLDNINGSBIDRAG_BARN", "Underholdningsbidrag til barn", "underholdsbidragTilBarn"),
    VENTELØNN("VENTELØNN", "Ventelønn", "venteloenn"),

    UDEFINERT("-", "UNDEFINED", null),
    ;

    private static final Map<String, UtbetaltYtelseFraOffentligeType> KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "YTELSE_FRA_OFFENTLIGE";

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonIgnore
    private String navn;

    private String kode;
    @JsonIgnore
    private String offisiellKode;

    private UtbetaltYtelseFraOffentligeType(String kode) {
        this.kode = kode;
    }

    private UtbetaltYtelseFraOffentligeType(String kode, String navn, String offisiellKode) {
        this.kode = kode;
        this.navn = navn;
        this.offisiellKode = offisiellKode;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static UtbetaltYtelseFraOffentligeType fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(UtbetaltYtelseFraOffentligeType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent OffentligYtelseType: " + kode);
        }
        return ad;
    }

    public static Map<String, UtbetaltYtelseFraOffentligeType> kodeMap() {
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

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getOffisiellKode() {
        return offisiellKode;
    }

    public static UtbetaltYtelseFraOffentligeType finnForKodeverkEiersKode(String offisiellDokumentType) {
        return List.of(values()).stream().filter(k -> Objects.equals(k.offisiellKode, offisiellDokumentType)).findFirst().orElse(UDEFINERT);
    }

    @Override
    public boolean erUdefinert() {
        return UDEFINERT.equals(this);
    }
}
