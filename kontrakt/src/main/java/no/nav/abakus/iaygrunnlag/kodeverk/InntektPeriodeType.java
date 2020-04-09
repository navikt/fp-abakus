package no.nav.abakus.iaygrunnlag.kodeverk;

import java.time.Period;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum InntektPeriodeType implements Kodeverdi {

    DAGLIG("DAGLG", "Daglig", "D", Period.ofDays(1)),
    UKENTLIG("UKNLG", "Ukentlig", "U", Period.ofWeeks(1)),
    BIUKENTLIG("14DLG", "Fjorten-daglig", "F", Period.ofWeeks(2)),
    MÅNEDLIG("MNDLG", "Månedlig", "M", Period.ofMonths(1)),
    ÅRLIG("AARLG", "Årlig", "Å", Period.ofYears(1)),
    FASTSATT25PAVVIK("INNFS", "Fastsatt etter 25 prosent avvik", "X", Period.ofYears(1)),
    PREMIEGRUNNLAG("PREMGR", "Premiegrunnlag", "Y", Period.ofYears(1)),
    UDEFINERT("-", "Ikke definert", null, null),
    ;
    
    /** @deprecated bruk enum konstant. */
    @Deprecated(forRemoval = true)
    public static final InntektPeriodeType PER_ÅR = ÅRLIG;
    /** @deprecated bruk enum konstant. */
    @Deprecated(forRemoval = true)
    public static final InntektPeriodeType PER_DAG = DAGLIG;
    /** @deprecated bruk enum konstant. */
    @Deprecated(forRemoval = true)
    public static final InntektPeriodeType PER_MÅNED = MÅNEDLIG;
    /** @deprecated bruk enum konstant. */
    @Deprecated(forRemoval = true)
    public static final InntektPeriodeType PER_UKE = UKENTLIG;
    /** @deprecated bruk enum konstant. */
    @Deprecated(forRemoval = true)
    public static final InntektPeriodeType PER_14DAGER = BIUKENTLIG;
    /** @deprecated bruk enum konstant. */
    @Deprecated(forRemoval = true)
    public static final InntektPeriodeType PREMIEGRUNNLAG_OPPDRAGSGIVER = PREMIEGRUNNLAG;
    /** @deprecated bruk enum konstant. */
    @Deprecated(forRemoval = true)
    public static final InntektPeriodeType FASTSATT_ETTER_AVVIKHÅNDTERING = FASTSATT25PAVVIK;

    private static final Map<String, InntektPeriodeType> KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "INNTEKT_PERIODE_TYPE";

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonIgnore
    private String navn;

    private String kode;
    @JsonIgnore
    private String offisiellKode;
    @JsonIgnore
    private Period periode;

    private InntektPeriodeType(String kode) {
        this.kode = kode;
    }

    private InntektPeriodeType(String kode, String navn, String offisiellKode, Period periode) {
        this.kode = kode;
        this.navn = navn;
        this.periode = periode;
        this.offisiellKode = offisiellKode;
    }

    @JsonCreator
    public static InntektPeriodeType fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent InntektPeriodeType: " + kode);
        }
        return ad;
    }

    public static Map<String, InntektPeriodeType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @Override
    public String getNavn() {
        return navn;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }

    public Period getPeriode() {
        return periode;
    }

    @Override
    public String getOffisiellKode() {
        return offisiellKode;
    }

}
