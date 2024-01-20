package no.nav.abakus.iaygrunnlag.kodeverk;

import java.time.Period;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonValue;

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

    private static final Map<String, InntektPeriodeType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private final String navn;

    @JsonValue
    private final String kode;

    private final String offisiellKode;

    private final Period periode;

    InntektPeriodeType(String kode, String navn, String offisiellKode, Period periode) {
        this.kode = kode;
        this.navn = navn;
        this.periode = periode;
        this.offisiellKode = offisiellKode;
    }

    public static InntektPeriodeType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode)).orElseThrow(() -> new IllegalArgumentException("Ukjent InntektPeriodeType: " + kode));
    }

    public static Map<String, InntektPeriodeType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public String getNavn() {
        return navn;
    }

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
