package no.nav.abakus.iaygrunnlag.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum SkatteOgAvgiftsregelType implements Kodeverdi {

    SÆRSKILT_FRADRAG_FOR_SJØFOLK("SÆRSKILT_FRADRAG_FOR_SJØFOLK", "Særskilt fradrag for sjøfolk", "saerskiltFradragForSjoefolk"),
    SVALBARD("SVALBARD", "Svalbardinntekt", "svalbard"),
    SKATTEFRI_ORGANISASJON("SKATTEFRI_ORGANISASJON", "Skattefri Organisasjon", "skattefriOrganisasjon"),
    NETTOLØNN_FOR_SJØFOLK("NETTOLØNN_FOR_SJØFOLK", "Nettolønn for sjøfolk", "nettoloennForSjoefolk"),
    NETTOLØNN("NETTOLØNN", "Nettolønn", "nettoloenn"),
    KILDESKATT_PÅ_PENSJONER("KILDESKATT_PÅ_PENSJONER", "Kildeskatt på pensjoner", "kildeskattPaaPensjoner"),
    JAN_MAYEN_OG_BILANDENE("JAN_MAYEN_OG_BILANDENE", "Inntekt på Jan Mayen og i norske biland i Antarktis", "janMayenOgBilandene"),

    UDEFINERT("-", "Udefinert", "Ikke definert"),
    ;

    private static final Map<String, SkatteOgAvgiftsregelType> KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "SKATTE_OG_AVGIFTSREGEL";

    @Deprecated
    public static final String DISCRIMINATOR = "SKATTE_OG_AVGIFTSREGEL";

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

    private SkatteOgAvgiftsregelType(String kode) {
        this.kode = kode;
    }

    private SkatteOgAvgiftsregelType(String kode, String navn, String offisiellKode) {
        this.kode = kode;
        this.navn = navn;
        this.offisiellKode = offisiellKode;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SkatteOgAvgiftsregelType fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdi(SkatteOgAvgiftsregelType.class, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent SkatteOgAvgiftsregelType: " + kode);
        }
        return ad;
    }

    public static Map<String, SkatteOgAvgiftsregelType> kodeMap() {
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

    @Override
    public String getOffisiellKode() {
        return offisiellKode;
    }

    public static SkatteOgAvgiftsregelType finnForKodeverkEiersKode(String offisiellDokumentType) {
        return List.of(values()).stream().filter(k -> Objects.equals(k.offisiellKode, offisiellDokumentType)).findFirst().orElse(UDEFINERT);
    }

}
