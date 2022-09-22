package no.nav.abakus.iaygrunnlag.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum OrganisasjonType implements Kodeverdi {

    JURIDISK_ENHET("JURIDISK_ENHET", "Juridisk enhet"),
    VIRKSOMHET("VIRKSOMHET", "Virksomhet"),
    ORGLEDD("ORGANISASJONSLEDD", "Organisasjonsledd"),
    KUNSTIG("KUNSTIG", "Kunstig arbeidsforhold lagt til av saksbehandler"),
    UDEFINERT("-", "Udefinert"),
    ;

    private static final Map<String, OrganisasjonType> KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "ORGANISASJONSTYPE";

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

    private OrganisasjonType(String kode) {
        this.kode = kode;
    }

    private OrganisasjonType(String kode, String navn) {
        this.kode = kode;
        this.navn = navn;
    }

    public static OrganisasjonType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode))
            .orElseThrow(() -> new IllegalArgumentException("Ukjent Organisasjonstype: " + kode));
    }

    public static Map<String, OrganisasjonType> kodeMap() {
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
