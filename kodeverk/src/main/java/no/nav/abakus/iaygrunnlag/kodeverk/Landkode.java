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
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public class Landkode implements Kodeverdi {
    private static final String KODEVERK = "LANDKODER";

    private static final Map<String, Landkode> KODER = initKoder();

    public static final Landkode NOR = fraKode("NOR"); //$NON-NLS-1$
    public static final Landkode DNK = fraKode("DNK"); //$NON-NLS-1$
    public static final Landkode SWE = fraKode("SWE"); //$NON-NLS-1$
    public static final Landkode USA = fraKode("USA"); //$NON-NLS-1$
    public static final Landkode PNG = fraKode("PNG"); //$NON-NLS-1$
    public static final Landkode BEL = fraKode("BEL"); //$NON-NLS-1$
    public static final Landkode FIN = fraKode("FIN"); //$NON-NLS-1$
    public static final Landkode CAN = fraKode("CAN"); //$NON-NLS-1$
    public static final Landkode ESP = fraKode("ESP"); //$NON-NLS-1$

    /** Kodeverkklient spesifikk konstant. Statsløs bruker */
    public static final Landkode STATSLØS = fraKode("XXX");

    /** Kodeverkklient spesifikk konstant. Bruker oppgir ikke land */
    public static final Landkode UOPPGITT_UKJENT = fraKode("???");

    /** Egendefinert konstant - ikke definert (null object pattern) for bruk i modeller som krever non-null. */
    public static final Landkode UDEFINERT = fraKode("-");

    public static final Landkode NORGE = NOR;
    public static final Landkode SVERIGE = SWE;
    public static final Landkode DANMARK = DNK;

    /** ISO 3166 alpha 3-letter code. */
    @JsonProperty(value = "kode")
    @Size(max = 3)
    @Pattern(regexp = "^[\\p{Alnum}]+$", message = "'${validatedValue}' matcher ikke tillatt pattern '{regexp}'")
    private String kode;

    Landkode() {
    }

    private Landkode(String kode) {
        this.kode = kode;
    }

    @Override
    public String getOffisiellKode() {
        return kode;
    }

    @Override
    public String getNavn() {
        return kode;
    }

    @Override
    public String getKode() {
        return kode;
    }

    @JsonProperty(value = "kodeverk", access = JsonProperty.Access.READ_ONLY)
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

    @JsonCreator
    public static Landkode fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
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
        map.put("-", new Landkode("-"));
        map.put("???", new Landkode("???"));
        map.put("XXX", new Landkode("XXX"));
        map.put("XXK", new Landkode("XXK"));

        return Collections.unmodifiableMap(map);
    }

    public static boolean erNorge(String kode) {
        return NOR.getKode().equals(kode);
    }

    public static Map<String, Landkode> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }
}
