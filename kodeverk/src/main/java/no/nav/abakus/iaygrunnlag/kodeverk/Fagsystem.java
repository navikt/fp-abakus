package no.nav.abakus.iaygrunnlag.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum Fagsystem implements Kodeverdi {

    BISYS("BISYS", "Bisys", "BID"),
    BIDRAGINNKREVING("BIDRAGINNKREVING", "Bidraginnkreving", "BII"),
    FPSAK("FPSAK", "Vedtaksløsning Foreldrepenger", "FS36"),
    FPABAKUS("FPABAKUS", "ABAKUS", "ABAKUS"),
    K9SAK("K9SAK", "Vedtaksløsning Folketrygdloven Kapittel 9", "K9SAK"),
    VLSP("VLSP", "Vedtaksløsning Sykepenger", "VLSP"),
    TPS("TPS", "TPS", "FS03"),
    JOARK("JOARK", "Joark", "AS36"),
    INFOTRYGD("INFOTRYGD", "Infotrygd", "IT01"),
    ARENA("ARENA", "Arena", "AO01"),
    INNTEKT("INNTEKT", "INNTEKT", "FS28"),
    MEDL("MEDL", "MEDL", "FS18"),
    GOSYS("GOSYS", "Gosys", "FS22"),
    GRISEN("GRISEN", "Grisen", "AO11"),
    GSAK("GSAK", "Gsak", "FS19"),
    HJE_HEL_ORT("HJE_HEL_ORT", "Hjelpemidler, Helsetjenester og Ort. Hjelpemidler", "OEBS"),
    ENHETSREGISTERET("ENHETSREGISTERET", "Enhetsregisteret", "ER01"),
    AAREGISTERET("AAREGISTERET", "AAregisteret", "AR01"),
    PESYS("PESYS", "Pesys", "PP01"),
    SKANNING("SKANNING", "Skanning", "MOT"),
    VENTELONN("VENTELONN", "Ventelønn", "V2"),
    UNNTAK("UNNTAK", "Unntak", "UFM"),
    ØKONOMI("ØKONOMI", "Økonomi", "OKO"),
    ØVRIG("ØVRIG", "ØVRIG", "OVR"),

    /**
     * Alle kodeverk må ha en verdi, det kan ikke være null i databasen. Denne koden gjør samme nytten.
     */
    UDEFINERT("-", "Ikke definert", null),
    ;

    public static final String KODEVERK = "FAGSYSTEM";

    private static final Map<String, Fagsystem> KODER = new LinkedHashMap<>();

    private String navn;

    private String offisiellKode;

    @JsonValue
    private String kode;

    Fagsystem() {
        // Hibernate trenger den
    }

    private Fagsystem(String kode) {
        this.kode = kode;
    }

    private Fagsystem(String kode, String navn, String offisiellKode) {
        this.kode = kode;
        this.navn = navn;
        this.offisiellKode = offisiellKode;
    }

    public static Fagsystem fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode))
            .orElseThrow(() -> new IllegalArgumentException("Ukjent Fagsystem: " + kode));
    }

    public static Map<String, Fagsystem> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @Override
    public String getNavn() {
        return navn;
    }

    @Override
    public String getOffisiellKode() {
        return offisiellKode;
    }

    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @Override
    public String getKode() {
        return kode;
    }

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }
}
