package no.nav.foreldrepenger.abakus.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public class Landkoder implements Kodeverdi {
    private static final String KODEVERK = "LANDKODER";

    private static final Map<String, Landkoder> KODER = initKoder();
    
    public static final Landkoder NOR = fraKode("NOR"); //$NON-NLS-1$
    public static final Landkoder SWE = fraKode("SWE"); //$NON-NLS-1$
    public static final Landkoder USA = fraKode("USA"); //$NON-NLS-1$
    public static final Landkoder PNG = fraKode("PNG"); //$NON-NLS-1$
    public static final Landkoder BEL = fraKode("BEL"); //$NON-NLS-1$
    public static final Landkoder FIN = fraKode("FIN"); //$NON-NLS-1$
    public static final Landkoder CAN = fraKode("CAN"); //$NON-NLS-1$
    public static final Landkoder ESP = fraKode("ESP"); //$NON-NLS-1$

    public static final Landkoder UDEFINERT = fraKode("-");  //$NON-NLS-1$
    
    /** ISO 3166 3-letter code. */
    private String kode;
    
    /** ISO 3166 2-letter code. */
    @Transient
    private String offisielIso2Kode;
    
    Landkoder() {
    }

    private Landkoder(String kode, String offisielIso2Kode) {
        this.kode = kode;
        this.offisielIso2Kode = offisielIso2Kode;
    }

    @Override
    public String getOffisiellKode() {
        return kode;
    }
    
    public String getOffisiellKodeISO2() {
        return offisielIso2Kode;
    }

    @Override
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
        if(obj==this) {
            return true;
        } else if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var other = (Landkoder) obj;
        return Objects.equals(kode, other.kode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kode);
    }
    
    @JsonCreator
    public static Landkoder fraKode(@JsonProperty("kode") String kode) {
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

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<Landkoder, String> {
        @Override
        public String convertToDatabaseColumn(Landkoder attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public Landkoder convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }

    private static Map<String, Landkoder> initKoder() {
        var map = new LinkedHashMap<String, Landkoder>();
        for(var c : Locale.getISOCountries()) {
            Locale locale = new Locale("", c);
            String iso3cc = locale.getISO3Country().toUpperCase();
            Landkoder landkode = new Landkoder(iso3cc, iso3cc);
            map.put(c, landkode);
            map.put(iso3cc, landkode);
        }
        map.put("-", new Landkoder("-", "Ikke definert"));
        return Collections.unmodifiableMap(map);
    }
    
    public static boolean erNorge(String kode) {
        return NOR.getKode().equals(kode);
    }

    public static Map<String, Landkoder> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }
}
