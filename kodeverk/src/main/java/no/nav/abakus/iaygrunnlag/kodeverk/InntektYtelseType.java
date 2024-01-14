package no.nav.abakus.iaygrunnlag.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum InntektYtelseType implements Kodeverdi {

    // Ytelse utbetalt til person som er arbeidstaker/frilanser/ytelsesmottaker
    AAP("AAP", "Arbeidsavklaringspenger", Kategori.YTELSE, YtelseType.ARBEIDSAVKLARINGSPENGER, "arbeidsavklaringspenger"),
    DAGPENGER("DAGPENGER", "Dagpenger arbeid og hyre", Kategori.YTELSE, YtelseType.DAGPENGER,
        List.of("dagpengerVedArbeidsloeshet", "dagpengerTilFiskerSomBareHarHyre")),
    FORELDREPENGER("FORELDREPENGER", "Foreldrepenger", Kategori.YTELSE, YtelseType.FORELDREPENGER, "foreldrepenger"),
    SVANGERSKAPSPENGER("SVANGERSKAPSPENGER", "Svangerskapspenger", Kategori.YTELSE, YtelseType.SVANGERSKAPSPENGER, "svangerskapspenger"),
    SYKEPENGER("SYKEPENGER", "Sykepenger", Kategori.YTELSE, YtelseType.SYKEPENGER,
        List.of("sykepenger", "sykepengerTilFiskerSomBareHarHyre")),
    OMSORGSPENGER("OMSORGSPENGER", "Omsorgspenger", Kategori.YTELSE, YtelseType.OMSORGSPENGER, "omsorgspenger"),
    OPPLÆRINGSPENGER("OPPLÆRINGSPENGER", "Opplæringspenger", Kategori.YTELSE, YtelseType.OPPLÆRINGSPENGER, "opplaeringspenger"),
    PLEIEPENGER("PLEIEPENGER", "Pleiepenger", Kategori.YTELSE, YtelseType.PLEIEPENGER_SYKT_BARN, "pleiepenger"),
    OVERGANGSSTØNAD_ENSLIG("OVERGANGSSTØNAD_ENSLIG", "Overgangsstønad til enslig mor eller far", Kategori.YTELSE, YtelseType.ENSLIG_FORSØRGER,
        "overgangsstoenadTilEnsligMorEllerFarSomBegynteAaLoepe1April2014EllerSenere"),
    VENTELØNN("VENTELØNN", "Ventelønn", Kategori.YTELSE, YtelseType.UDEFINERT, "venteloenn"),

    // Feriepenger Ytelse utbetalt til person som er arbeidstaker/frilanser/ytelsesmottaker
    // TODO slå sammen til FERIEPENGER_YTELSE - eller ta de med under hver ytelse???
    FERIEPENGER_FORELDREPENGER("FERIEPENGER_FORELDREPENGER", "Feriepenger foreldrepenger", Kategori.YTELSE, YtelseType.FORELDREPENGER, "feriepengerForeldrepenger"),
    FERIEPENGER_SVANGERSKAPSPENGER("FERIEPENGER_SVANGERSKAPSPENGER", "Feriepenger svangerskapspenger", Kategori.YTELSE, YtelseType.SVANGERSKAPSPENGER, "feriepengerSvangerskapspenger"),
    FERIEPENGER_OMSORGSPENGER("FERIEPENGER_OMSORGSPENGER", "Feriepenger omsorgspenger", Kategori.YTELSE, YtelseType.OMSORGSPENGER, "feriepengerOmsorgspenger"),
    FERIEPENGER_OPPLÆRINGSPENGER("FERIEPENGER_OPPLÆRINGSPENGER", "Feriepenger opplæringspenger", Kategori.YTELSE, YtelseType.OPPLÆRINGSPENGER, "feriepengerOpplaeringspenger"),
    FERIEPENGER_PLEIEPENGER("FERIEPENGER_PLEIEPENGER", "Feriepenger pleiepenger", Kategori.YTELSE, YtelseType.PLEIEPENGER_SYKT_BARN, "feriepengerPleiepenger"),
    FERIEPENGER_SYKEPENGER("FERIEPENGER_SYKEPENGER", "Feriepenger sykepenger", Kategori.YTELSE, YtelseType.SYKEPENGER,
        List.of("feriepengerSykepenger", "feriepengerSykepengerTilFiskerSomBareHarHyre")),
    FERIETILLEGG_DAGPENGER("FERIETILLEGG_DAGPENGER", "Ferietillegg dagpenger ", Kategori.YTELSE, YtelseType.DAGPENGER,
        List.of("ferietilleggDagpengerVedArbeidsloeshet", "ferietilleggDagpengerTilFiskerSomBareHarHyre")),

    // Annen ytelse utbetalt til person
    KVALIFISERINGSSTØNAD("KVALIFISERINGSSTØNAD", "Kvalifiseringsstønad", Kategori.TRYGD, YtelseType.UDEFINERT, "kvalifiseringstoenad"),

    // Ytelse utbetalt til person som er næringsdrivende, fisker/lott, dagmamma eller jord/skogbruker
    FORELDREPENGER_NÆRING("FORELDREPENGER_NÆRING", "Foreldrepenger næring", Kategori.NÆRING, YtelseType.FORELDREPENGER,
        List.of("foreldrepenger", "foreldrepengerTilDagmamma", "foreldrepengerTilFisker", "foreldrepengerTilJordOgSkogbrukere")),
    SVANGERSKAPSPENGER_NÆRING("SVANGERSKAPSPENGER_NÆRING", "Svangerskapspenger næring", Kategori.NÆRING, YtelseType.SVANGERSKAPSPENGER,
        List.of("svangerskapspenger", "svangerskapspengerTilDagmamma", "svangerskapspengerTilFisker", "svangerskapspengerTilJordOgSkogbrukere")),
    SYKEPENGER_NÆRING("SYKEPENGER_NÆRING", "Sykepenger næring", Kategori.NÆRING, YtelseType.SYKEPENGER,
        List.of("sykepenger", "sykepengerTilDagmamma", "sykepengerTilFisker", "sykepengerTilJordOgSkogbrukere")),
    OMSORGSPENGER_NÆRING("OMSORGSPENGER_NÆRING", "Omsorgspenger næring", Kategori.NÆRING, YtelseType.OMSORGSPENGER,
        List.of("omsorgspenger", "omsorgspengerTilDagmamma", "omsorgspengerTilFisker", "omsorgspengerTilJordOgSkogbrukere")),
    OPPLÆRINGSPENGER_NÆRING("OPPLÆRINGSPENGER_NÆRING", "Opplæringspenger næring", Kategori.NÆRING, YtelseType.OPPLÆRINGSPENGER,
        List.of("opplaeringspenger", "opplaeringspengerTilDagmamma", "opplaeringspengerTilFisker", "opplaeringspengerTilJordOgSkogbrukere")),
    PLEIEPENGER_NÆRING("PLEIEPENGER_NÆRING", "Pleiepenger næring", Kategori.NÆRING, YtelseType.PLEIEPENGER_SYKT_BARN,
        List.of("pleiepenger", "pleiepengerTilDagmamma", "pleiepengerTilFisker", "pleiepengerTilJordOgSkogbrukere")),
    DAGPENGER_NÆRING("DAGPENGER_NÆRING", "Dagpenger næring", Kategori.NÆRING, YtelseType.DAGPENGER,
        List.of("dagpengerVedArbeidsloeshet", "dagpengerTilFisker")),

    // Annen ytelse utbetalt til person som er næringsdrivende
    ANNET("ANNET", "Annet", Kategori.NÆRING, YtelseType.UDEFINERT, "annet"),
    VEDERLAG("VEDERLAG", "Vederlag", Kategori.NÆRING, YtelseType.UDEFINERT, List.of("vederlag", "vederlagDagmammaIEgetHjem")),
    LOTT_KUN_TRYGDEAVGIFT("LOTT_KUN_TRYGDEAVGIFT", "Lott kun trygdeavgift", Kategori.NÆRING, YtelseType.UDEFINERT, "lottKunTrygdeavgift"),
    KOMPENSASJON_FOR_TAPT_PERSONINNTEKT("KOMPENSASJON_FOR_TAPT_PERSONINNTEKT", "Kompensasjon for tapt personinntekt", Kategori.NÆRING, YtelseType.UDEFINERT, "kompensasjonForTaptPersoninntekt"),


    UDEFINERT("-", "UNDEFINED", Kategori.ALLE, YtelseType.UDEFINERT, List.of()),
    ;

    public static final String KODEVERK = "YTELSE_INNTEKT_TYPE";
    private static final Map<String, InntektYtelseType> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }


    @JsonValue
    private String kode;

    private final String navn;
    private final YtelseType ytelseType;

    private final Kategori kategori;
    private final List<String> offisiellKode;

    InntektYtelseType(String kode, String navn, Kategori kategori, YtelseType ytelseType, String offisiellKode) {
        this(kode, navn, kategori, ytelseType, List.of(offisiellKode));
    }

    InntektYtelseType(String kode, String navn, Kategori kategori, YtelseType ytelseType, List<String> offisiellKode) {
        this.kode = kode;
        this.navn = navn;
        this.kategori = kategori;
        this.ytelseType = ytelseType;
        this.offisiellKode = offisiellKode != null ? offisiellKode : List.of();
    }

    public static InntektYtelseType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode)).orElseThrow(() -> new IllegalArgumentException("Ukjent YtelseInntektType: " + kode));
    }

    public static Map<String, InntektYtelseType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public static InntektYtelseType finnForKodeverkEiersKode(Kategori kategori, String kode) {
        return Stream.of(values()).filter(k -> k.kategori == kategori && k.offisiellKode.contains(kode)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Ukjent YtelseInntektType: " + kode + " kategori " + kategori));
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

    @Override
    public String getOffisiellKode() {
        return offisiellKode.stream().findFirst().orElse(null);
    }

    public YtelseType getYtelseType() {
        return ytelseType;
    }

    private boolean erOrdinærYtelse() {
        return kategori == Kategori.YTELSE || kategori == Kategori.ALLE;
    }

    private boolean erNæringsYtelse() {
        return kategori == Kategori.NÆRING || kategori == Kategori.ALLE;
    }

    public enum Kategori { YTELSE, NÆRING, TRYGD, ALLE }
}
