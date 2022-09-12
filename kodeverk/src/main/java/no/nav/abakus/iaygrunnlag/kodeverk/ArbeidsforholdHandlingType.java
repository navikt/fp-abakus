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
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum ArbeidsforholdHandlingType implements Kodeverdi {

    UDEFINERT("-", "Udefinert"),
    BRUK("BRUK", "Bruk"),
    NYTT_ARBEIDSFORHOLD("NYTT_ARBEIDSFORHOLD", "Arbeidsforholdet er ansett som nytt"),
    BRUK_UTEN_INNTEKTSMELDING("BRUK_UTEN_INNTEKTSMELDING", "Bruk, men ikke benytt inntektsmelding"),
    IKKE_BRUK("IKKE_BRUK", "Ikke bruk"),
    SLÅTT_SAMMEN_MED_ANNET("SLÅTT_SAMMEN_MED_ANNET", "Arbeidsforholdet er slått sammen med et annet"),
    LAGT_TIL_AV_SAKSBEHANDLER("LAGT_TIL_AV_SAKSBEHANDLER", "Arbeidsforhold lagt til av saksbehandler"),
    BASERT_PÅ_INNTEKTSMELDING("BASERT_PÅ_INNTEKTSMELDING", "Arbeidsforholdet som ikke ligger i AA-reg er basert på inntektsmelding"),
    BRUK_MED_OVERSTYRT_PERIODE("BRUK_MED_OVERSTYRT_PERIODE", "Bruk arbeidsforholdet med overstyrt periode"),
    INNTEKT_IKKE_MED_I_BG("INNTEKT_IKKE_MED_I_BG", "Inntekten til arbeidsforholdet skal ikke være med i beregningsgrunnlaget"),
    ;

    private static final Map<String, ArbeidsforholdHandlingType> KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "ARBEIDSFORHOLD_HANDLING_TYPE";

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

    private ArbeidsforholdHandlingType(String kode) {
        this.kode = kode;
    }

    private ArbeidsforholdHandlingType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static ArbeidsforholdHandlingType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode))
            .orElseThrow(() -> new IllegalArgumentException("Ukjent ArbeidsforholdHandlingType: " + kode));
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

}
