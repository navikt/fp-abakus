package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public enum TekniskNavn {

    PERSONINNTEKT_LØNN("personinntektLoenn", InntektspostType.LØNN),
    PERSONINNTEKT_BARE_PENSJONSDEL("personinntektBarePensjonsdel", InntektspostType.LØNN),
    PERSONINNTEKT_NÆRING("personinntektNaering", InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE),
    PERSONINNTEKT_FISKE_FANGST_FAMILIEBARNEHAGE("personinntektFiskeFangstFamiliebarnehage", InntektspostType.NÆRING_FISKE_FANGST_FAMBARNEHAGE),
    SVALBARD_LØNN_LØNNSTREKKORDNINGEN("svalbardLoennLoennstrekkordningen", InntektspostType.LØNN),
    SVALBARD_PERSONINNTEKT_NÆRING("svalbardPersoninntektNaering", InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE),
    LØNNSINNTEKT_MED_TRYGDEAVGIFTSPLIKT_OMFATTET_AV_LØNNSTREKKORDNINGEN("loennsinntektMedTrygdeavgiftspliktOmfattetAvLoennstrekkordningen", InntektspostType.LØNN),
    SKATTEOPPGJØRSDATO("skatteoppgjoersdato", null);

    private static final String KODEVERK = "TEKNISK_NAVN";

    private static final Map<String, TekniskNavn> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private final String kode;
    private final InntektspostType inntektspostType;


    TekniskNavn(String kode, InntektspostType inntektspostType) {
        this.kode = kode;
        this.inntektspostType = inntektspostType;
    }

    @JsonCreator
    public static TekniskNavn fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        return KODER.get(kode);
    }

    public String getKode() {
        return kode;
    }

    public String getKodeverk() {
        return KODEVERK;
    }

    public InntektspostType getInntektspostType() {
        return inntektspostType;
    }
}
