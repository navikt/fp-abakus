package no.nav.abakus.iaygrunnlag.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Locale.IsoCountryCode;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public class Landkode implements Kodeverdi {
    private static final String KODEVERK = "LANDKODER";

    private static final Map<String, Landkode> KODER = initKoder();

    public static final Landkode NOR = fraKode("NOR"); //$NON-NLS-1$
    public static final Landkode DNK = fraKode("DNK"); //$NON-NLS-1$
    public static final Landkode SWE = fraKode("SWE"); //$NON-NLS-1$

    /** Egendefinert konstant - ikke definert (null object pattern) for bruk i modeller som krever non-null. */
    public static final Landkode UDEFINERT = fraKode("-");

    /** ISO 3166 alpha 3-letter code. */
    @JsonValue
    @Size(max = 3)
    @Pattern(regexp = "^[\\p{Alnum}]+$", message = "[${validatedValue}] matcher ikke tillatt pattern [{regexp}]")
    private String kode;

    Landkode() {
    }

    private Landkode(String kode) {
        this.kode = kode;
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

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Landkode fraKode(@JsonProperty(value = "kode") Object node) {
        if (node == null) {
            return null;
        }
        String kode = TempAvledeKode.getVerdiLandKoder(KODEVERK, node, "kode");
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Landkode: " + kode);
        }
        return ad;
    }

    @Override
    public String toString() {
        return kode;
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
}
