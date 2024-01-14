package no.nav.abakus.iaygrunnlag.kodeverk;

import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum InntektYtelseType implements Kodeverdi {

    // Ytelse utbetalt til person som er arbeidstaker/frilanser/ytelsesmottaker
    AAP("Arbeidsavklaringspenger", Kategori.YTELSE, YtelseType.ARBEIDSAVKLARINGSPENGER, "arbeidsavklaringspenger"),
    DAGPENGER("Dagpenger arbeid og hyre", Kategori.YTELSE, YtelseType.DAGPENGER,
        List.of("dagpengerVedArbeidsloeshet", "dagpengerTilFiskerSomBareHarHyre")),
    FORELDREPENGER("Foreldrepenger", Kategori.YTELSE, YtelseType.FORELDREPENGER, "foreldrepenger"),
    SVANGERSKAPSPENGER("Svangerskapspenger", Kategori.YTELSE, YtelseType.SVANGERSKAPSPENGER, "svangerskapspenger"),
    SYKEPENGER("Sykepenger", Kategori.YTELSE, YtelseType.SYKEPENGER,
        List.of("sykepenger", "sykepengerTilFiskerSomBareHarHyre")),
    OMSORGSPENGER("Omsorgspenger", Kategori.YTELSE, YtelseType.OMSORGSPENGER, "omsorgspenger"),
    OPPLÆRINGSPENGER("Opplæringspenger", Kategori.YTELSE, YtelseType.OPPLÆRINGSPENGER, "opplaeringspenger"),
    PLEIEPENGER("Pleiepenger", Kategori.YTELSE, YtelseType.PLEIEPENGER_SYKT_BARN, "pleiepenger"),
    OVERGANGSSTØNAD_ENSLIG("Overgangsstønad til enslig mor eller far", Kategori.YTELSE, YtelseType.ENSLIG_FORSØRGER,
        "overgangsstoenadTilEnsligMorEllerFarSomBegynteAaLoepe1April2014EllerSenere"),
    VENTELØNN("Ventelønn", Kategori.YTELSE, YtelseType.UDEFINERT, "venteloenn"),

    // Feriepenger Ytelse utbetalt til person som er arbeidstaker/frilanser/ytelsesmottaker
    // TODO slå sammen til FERIEPENGER_YTELSE - eller ta de med under hver ytelse???
    FERIEPENGER_FORELDREPENGER("Feriepenger foreldrepenger", Kategori.YTELSE, YtelseType.FORELDREPENGER, "feriepengerForeldrepenger"),
    FERIEPENGER_SVANGERSKAPSPENGER("Feriepenger svangerskapspenger", Kategori.YTELSE, YtelseType.SVANGERSKAPSPENGER, "feriepengerSvangerskapspenger"),
    FERIEPENGER_OMSORGSPENGER("Feriepenger omsorgspenger", Kategori.YTELSE, YtelseType.OMSORGSPENGER, "feriepengerOmsorgspenger"),
    FERIEPENGER_OPPLÆRINGSPENGER("Feriepenger opplæringspenger", Kategori.YTELSE, YtelseType.OPPLÆRINGSPENGER, "feriepengerOpplaeringspenger"),
    FERIEPENGER_PLEIEPENGER("Feriepenger pleiepenger", Kategori.YTELSE, YtelseType.PLEIEPENGER_SYKT_BARN, "feriepengerPleiepenger"),
    FERIEPENGER_SYKEPENGER("Feriepenger sykepenger", Kategori.YTELSE, YtelseType.SYKEPENGER,
        List.of("feriepengerSykepenger", "feriepengerSykepengerTilFiskerSomBareHarHyre")),
    FERIETILLEGG_DAGPENGER("Ferietillegg dagpenger ", Kategori.YTELSE, YtelseType.DAGPENGER,
        List.of("ferietilleggDagpengerVedArbeidsloeshet", "ferietilleggDagpengerTilFiskerSomBareHarHyre")),

    // Annen ytelse utbetalt til person
    KVALIFISERINGSSTØNAD("Kvalifiseringsstønad", Kategori.TRYGD, YtelseType.UDEFINERT, "kvalifiseringstoenad"),

    // Ytelse utbetalt til person som er næringsdrivende, fisker/lott, dagmamma eller jord/skogbruker
    FORELDREPENGER_NÆRING("Foreldrepenger næring", Kategori.NÆRING, YtelseType.FORELDREPENGER,
        List.of("foreldrepenger", "foreldrepengerTilDagmamma", "foreldrepengerTilFisker", "foreldrepengerTilJordOgSkogbrukere")),
    SVANGERSKAPSPENGER_NÆRING("Svangerskapspenger næring", Kategori.NÆRING, YtelseType.SVANGERSKAPSPENGER,
        List.of("svangerskapspenger", "svangerskapspengerTilDagmamma", "svangerskapspengerTilFisker", "svangerskapspengerTilJordOgSkogbrukere")),
    SYKEPENGER_NÆRING("Sykepenger næring", Kategori.NÆRING, YtelseType.SYKEPENGER,
        List.of("sykepenger", "sykepengerTilDagmamma", "sykepengerTilFisker", "sykepengerTilJordOgSkogbrukere")),
    OMSORGSPENGER_NÆRING("Omsorgspenger næring", Kategori.NÆRING, YtelseType.OMSORGSPENGER,
        List.of("omsorgspenger", "omsorgspengerTilDagmamma", "omsorgspengerTilFisker", "omsorgspengerTilJordOgSkogbrukere")),
    OPPLÆRINGSPENGER_NÆRING("Opplæringspenger næring", Kategori.NÆRING, YtelseType.OPPLÆRINGSPENGER,
        List.of("opplaeringspenger", "opplaeringspengerTilDagmamma", "opplaeringspengerTilFisker", "opplaeringspengerTilJordOgSkogbrukere")),
    PLEIEPENGER_NÆRING("Pleiepenger næring", Kategori.NÆRING, YtelseType.PLEIEPENGER_SYKT_BARN,
        List.of("pleiepenger", "pleiepengerTilDagmamma", "pleiepengerTilFisker", "pleiepengerTilJordOgSkogbrukere")),
    DAGPENGER_NÆRING("Dagpenger næring", Kategori.NÆRING, YtelseType.DAGPENGER,
        List.of("dagpengerVedArbeidsloeshet", "dagpengerTilFisker")),

    // Annen ytelse utbetalt til person som er næringsdrivende
    ANNET("Annet", Kategori.NÆRING, YtelseType.UDEFINERT, "annet"),
    VEDERLAG("Vederlag", Kategori.NÆRING, YtelseType.UDEFINERT, List.of("vederlag", "vederlagDagmammaIEgetHjem")),
    LOTT_KUN_TRYGDEAVGIFT("Lott kun trygdeavgift", Kategori.NÆRING, YtelseType.UDEFINERT, "lottKunTrygdeavgift"),
    KOMPENSASJON_FOR_TAPT_PERSONINNTEKT("Kompensasjon for tapt personinntekt", Kategori.NÆRING, YtelseType.UDEFINERT, "kompensasjonForTaptPersoninntekt")
    ;

    public static final String KODEVERK = "INNTEKT_YTELSE_TYPE";

    private final String navn;
    private final YtelseType ytelseType;

    private final Kategori kategori;
    private final List<String> offisiellKode;

    InntektYtelseType(String navn, Kategori kategori, YtelseType ytelseType, String offisiellKode) {
        this(navn, kategori, ytelseType, List.of(offisiellKode));
    }

    InntektYtelseType(String navn, Kategori kategori, YtelseType ytelseType, List<String> offisiellKode) {
        this.navn = navn;
        this.kategori = kategori;
        this.ytelseType = ytelseType;
        this.offisiellKode = offisiellKode != null ? offisiellKode : List.of();
    }

    public static InntektYtelseType fraKode(String kode) {
        return kode != null ? InntektYtelseType.valueOf(kode) : null;
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
        return name();
    }

    @Override
    public String getOffisiellKode() {
        return offisiellKode.stream().findFirst().orElse(null);
    }

    public YtelseType getYtelseType() {
        return ytelseType;
    }

    private boolean erOrdinærYtelse() {
        return kategori == Kategori.YTELSE;
    }

    private boolean erNæringsYtelse() {
        return kategori == Kategori.NÆRING;
    }

    public enum Kategori { YTELSE, NÆRING, TRYGD }
}
