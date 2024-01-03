package no.nav.abakus.iaygrunnlag.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Locale.IsoCountryCode;
import java.util.Map;
import java.util.Objects;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public class Landkode implements Kodeverdi {
    private static final String KODEVERK = "LANDKODER";

    private static final Map<String, Landkode> KODER = initKoder();

    public static final Landkode NOR = fraKode("NOR");
    public static final Landkode DNK = fraKode("DNK");
    public static final Landkode SWE = fraKode("SWE");

    /**
     * Egendefinert konstant - ikke definert (null object pattern) for bruk i modeller som krever non-null.
     */
    public static final Landkode UDEFINERT = fraKode("-");

    /**
     * ISO 3166 alpha 3-letter code.
     */
    @JsonValue
    @Size(max = 3)
    @Pattern(regexp = "^[\\p{Alnum}]+$", message = "[${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
    private String kode;

    Landkode() {
    }

    private Landkode(String kode) {
        this.kode = kode;
    }

    @JsonCreator
    public static Landkode fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Landkode: " + kode);
        }
        return ad;
    }

    private static Map<String, Landkode> initKoder() {
        var map = new LinkedHashMap<String, Landkode>();
        for (var iso2cc : Locale.getISOCountries(IsoCountryCode.PART1_ALPHA2)) {
            Locale locale = new Locale("", iso2cc);
            String iso3cc = locale.getISO3Country().toUpperCase();
            Landkode landkode = new Landkode(iso3cc);
            map.put(iso2cc, landkode);
            map.put(iso3cc, landkode);
        }
        /** Udefinert */
        map.put("-", new Landkode("-"));
        /** Kodeverkklient spesifikk konstant. Bruker oppgir ikke land */
        map.put("???", new Landkode("???"));
        /** Kodeverkklient spesifikk konstant. Statsløs bruker */
        map.put("XXX", new Landkode("XXX"));
        map.put("XXK", new Landkode("XXK"));

        return Collections.unmodifiableMap(map);
    }

    public String getNavn() {
        return kode;
    }

    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var other = (Landkode) obj;
        return Objects.equals(kode, other.kode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kode);
    }

    @Override
    public String toString() {
        return kode;
    }
}
