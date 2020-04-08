package no.nav.foreldrepenger.abakus.vedtak.domene;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import no.nav.abakus.iaygrunnlag.kodeverk.Kodeverdi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Gammelt kodeverk fra infotrygd. Burde ikke lekke fra Abakus (bør fjernes fra iay kontrakt og der heller benytte FagsakYtelseType).
 * Kan fjernes når vi ikke lenger er avhengig av migrering fra infotrygd.
 */
@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum TemaUnderkategori implements Kodeverdi {

    FORELDREPENGER("FP", "Foreldrepenger", "FP"),
    FORELDREPENGER_FODSEL("FØ", "Foreldrepenger fødsel", "FØ"),
    FORELDREPENGER_ADOPSJON("AP", "Foreldrepenger adopsjon", "AP"),
    FORELDREPENGER_SVANGERSKAPSPENGER("SV", "Svangerskapspenger", "SV"),
    SYKEPENGER_SYKEPENGER("SP", "Sykepenger", "SP"),
    PÅRØRENDE_OMSORGSPENGER("OM", "Pårørende omsorgsmpenger", "OM"),
    PÅRØRENDE_OPPLÆRINGSPENGER("OP", "Pårørende opplæringspenger", "OP"),
    PÅRØRENDE_PLEIETRENGENDE_SYKT_BARN("PB", "Pårørende pleietrengende sykt barn", "PB"),
    PÅRØRENDE_PLEIETRENGENDE("PI", "Pårørende pleietrengende", "PI"),
    PÅRØRENDE_PLEIETRENGENDE_PÅRØRENDE("PP", "Pårørende pleietrengende pårørende", "PP"),
    PÅRØRENDE_PLEIEPENGER("PN", "Pårørende pleiepenger", "PN"),
    SYKEPENGER_FORSIKRINGSRISIKO("SU", "Sykepenger utenlandsopphold", "SU"),
    SYKEPENGER_REISETILSKUDD("RT", "Reisetilskudd", "RT"),
    SYKEPENGER_UTENLANDSOPPHOLD("RS", "Forsikr.risiko sykefravær", "RS"),
    OVERGANGSSTØNAD("OG", "Overgangsstønad", "OG"),
    FORELDREPENGER_FODSEL_UTLAND("FU", "Foreldrepenger fødsel, utland", "FU"),
    ENGANGSSTONAD_ADOPSJON("AE", "Adopsjon engangsstønad", "AE"),
    ENGANGSSTONAD_FODSEL("FE", "Fødsel engangsstønad", "FE"),

    BT("BT", "Stønad til barnetilsyn", "BT"),
    FL("FL", "Tilskudd til flytting", "FL"),
    UT("UT", "Skolepenger", "UT"),

    UDEFINERT("-", "Udefinert", null),
    ;

    private static final Map<String, TemaUnderkategori> KODER = new LinkedHashMap<>();

    private static final String KODEVERK = "TEMA_UNDERKATEGORI";

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

    private TemaUnderkategori(String kode) {
        this.kode = kode;
    }

    private TemaUnderkategori(String kode, String navn, String offisiellKode) {
        this.kode = kode;
        this.navn = navn;
        this.offisiellKode = offisiellKode;
    }

    @JsonCreator
    public static TemaUnderkategori fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent TemaUnderkategori: " + kode);
        }
        return ad;
    }

    public static Map<String, TemaUnderkategori> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    @Override
    public String getNavn() {
        return navn;
    }

    @JsonProperty
    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @JsonProperty
    @Override
    public String getKode() {
        return kode;
    }

    @Override
    public String getOffisiellKode() {
        return offisiellKode;
    }


    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<TemaUnderkategori, String> {
        @Override
        public String convertToDatabaseColumn(TemaUnderkategori attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public TemaUnderkategori convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }

    private static final List<TemaUnderkategori> FORELDREPENGER_BEHANDLINGSTEMAER = Arrays.asList(
        FORELDREPENGER, FORELDREPENGER_FODSEL, FORELDREPENGER_ADOPSJON, FORELDREPENGER_FODSEL_UTLAND);

    private static final List<TemaUnderkategori> ENGANGSSTONAD_BEHANDLINGSTEMAER = Arrays.asList(ENGANGSSTONAD_ADOPSJON,
        ENGANGSSTONAD_FODSEL);


    public static boolean erGjelderEngangsstonad(String underkategori) {
        return ENGANGSSTONAD_BEHANDLINGSTEMAER.stream()
            .anyMatch(temaUnderkategori -> temaUnderkategori.getKode().equals(underkategori));
    }

    public static boolean erGjelderSvangerskapspenger(String underkategori) {
        return FORELDREPENGER_SVANGERSKAPSPENGER.getKode().equals(underkategori);
    }

    public static boolean erGjelderForeldrepenger(String underkategori) {
        return FORELDREPENGER_BEHANDLINGSTEMAER.stream()
            .anyMatch(temaUnderkategori -> temaUnderkategori.getKode().equals(underkategori));
    }
}
