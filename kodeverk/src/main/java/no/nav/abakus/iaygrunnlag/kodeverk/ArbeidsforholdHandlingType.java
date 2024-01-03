package no.nav.abakus.iaygrunnlag.kodeverk;

/**
 * <h3>Internt kodeverk</h3>
 * Definerer typer av handlinger en saksbehandler kan gjøre vedrørende et arbeidsforhold
 * <p>
 */

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum ArbeidsforholdHandlingType implements Kodeverdi {

    UDEFINERT("-", "Udefinert", false),
    BRUK("BRUK", "Bruk", false),
    NYTT_ARBEIDSFORHOLD("NYTT_ARBEIDSFORHOLD", "Arbeidsforholdet er ansett som nytt", false),
    BRUK_UTEN_INNTEKTSMELDING("BRUK_UTEN_INNTEKTSMELDING", "Bruk, men ikke benytt inntektsmelding", false),
    IKKE_BRUK("IKKE_BRUK", "Ikke bruk", false),
    /**
     * @deprecated Tillater ikke nye sammenslåinger.  Logikken var del av 5080 i FP, vil fases ut. Sist brukt 2019-11, støttes for legacy grunnlag inntil videre
     */
    @Deprecated(forRemoval = true)
    SLÅTT_SAMMEN_MED_ANNET("SLÅTT_SAMMEN_MED_ANNET", "Arbeidsforholdet er slått sammen med et annet", true),

    LAGT_TIL_AV_SAKSBEHANDLER("LAGT_TIL_AV_SAKSBEHANDLER", "Arbeidsforhold lagt til av saksbehandler", false),
    BASERT_PÅ_INNTEKTSMELDING("BASERT_PÅ_INNTEKTSMELDING", "Arbeidsforholdet som ikke ligger i AA-reg er basert på inntektsmelding", false),
    BRUK_MED_OVERSTYRT_PERIODE("BRUK_MED_OVERSTYRT_PERIODE", "Bruk arbeidsforholdet med overstyrt periode", false),
    INNTEKT_IKKE_MED_I_BG("INNTEKT_IKKE_MED_I_BG", "Inntekten til arbeidsforholdet skal ikke være med i beregningsgrunnlaget", false),
    ;

    public static final String KODEVERK = "ARBEIDSFORHOLD_HANDLING_TYPE";
    private static final Map<String, ArbeidsforholdHandlingType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private String navn;

    @JsonIgnore
    private boolean readOnly;

    @JsonValue
    private String kode;

    private ArbeidsforholdHandlingType(String kode, String navn, boolean readOnly) {
        this.kode = kode;
        this.navn = navn;
        this.readOnly = readOnly;
    }

    public static ArbeidsforholdHandlingType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode)).orElseThrow(() -> new IllegalArgumentException("Ukjent ArbeidsforholdHandlingType: " + kode));
    }

    public static Map<String, ArbeidsforholdHandlingType> kodeMap() {
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

    public boolean isReadOnly() {
        return readOnly;
    }

}
