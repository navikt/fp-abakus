package no.nav.abakus.iaygrunnlag.kodeverk;

import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum InntektYtelseType implements Kodeverdi {

    // Ytelse utbetalt til person som er arbeidstaker/frilanser/ytelsesmottaker
    AAP("Arbeidsavklaringspenger", Kategori.YTELSE, "arbeidsavklaringspenger"),
    DAGPENGER("Dagpenger arbeid og hyre", Kategori.YTELSE, List.of("dagpengerVedArbeidsloeshet", "dagpengerTilFiskerSomBareHarHyre")),
    FORELDREPENGER("Foreldrepenger", Kategori.YTELSE, "foreldrepenger"),
    SVANGERSKAPSPENGER("Svangerskapspenger", Kategori.YTELSE, "svangerskapspenger"),
    SYKEPENGER("Sykepenger", Kategori.YTELSE, List.of("sykepenger", "sykepengerTilFiskerSomBareHarHyre")),
    OMSORGSPENGER("Omsorgspenger", Kategori.YTELSE, "omsorgspenger"),
    OPPLÆRINGSPENGER("Opplæringspenger", Kategori.YTELSE, "opplaeringspenger"),
    PLEIEPENGER("Pleiepenger", Kategori.YTELSE, "pleiepenger"),
    OVERGANGSSTØNAD_ENSLIG("Overgangsstønad til enslig mor eller far", Kategori.YTELSE,
        "overgangsstoenadTilEnsligMorEllerFarSomBegynteAaLoepe1April2014EllerSenere"),
    VENTELØNN("Ventelønn", Kategori.YTELSE, "venteloenn"),

    // Feriepenger Ytelse utbetalt til person som er arbeidstaker/frilanser/ytelsesmottaker
    // TODO slå sammen til FERIEPENGER_YTELSE - eller ta de med under hver ytelse???
    FERIEPENGER_FORELDREPENGER("Feriepenger foreldrepenger", Kategori.YTELSE, "feriepengerForeldrepenger"),
    FERIEPENGER_SVANGERSKAPSPENGER("Feriepenger svangerskapspenger", Kategori.YTELSE, "feriepengerSvangerskapspenger"),
    FERIEPENGER_OMSORGSPENGER("Feriepenger omsorgspenger", Kategori.YTELSE, "feriepengerOmsorgspenger"),
    FERIEPENGER_OPPLÆRINGSPENGER("Feriepenger opplæringspenger", Kategori.YTELSE, "feriepengerOpplaeringspenger"),
    FERIEPENGER_PLEIEPENGER("Feriepenger pleiepenger", Kategori.YTELSE, "feriepengerPleiepenger"),
    FERIEPENGER_SYKEPENGER("Feriepenger sykepenger", Kategori.YTELSE, List.of("feriepengerSykepenger", "feriepengerSykepengerTilFiskerSomBareHarHyre")),
    FERIETILLEGG_DAGPENGER("Ferietillegg dagpenger ", Kategori.YTELSE, List.of("ferietilleggDagpengerVedArbeidsloeshet", "ferietilleggDagpengerTilFiskerSomBareHarHyre")),

    // Annen ytelse utbetalt til person
    KVALIFISERINGSSTØNAD("Kvalifiseringsstønad", Kategori.TRYGD, "kvalifiseringstoenad"),

    // Ytelse utbetalt til person som er næringsdrivende, fisker/lott, dagmamma eller jord/skogbruker
    FORELDREPENGER_NÆRING("Foreldrepenger næring", Kategori.NÆRING, List.of("foreldrepenger", "foreldrepengerTilDagmamma", "foreldrepengerTilFisker", "foreldrepengerTilJordOgSkogbrukere")),
    SVANGERSKAPSPENGER_NÆRING("Svangerskapspenger næring", Kategori.NÆRING, List.of("svangerskapspenger", "svangerskapspengerTilDagmamma", "svangerskapspengerTilFisker", "svangerskapspengerTilJordOgSkogbrukere")),
    SYKEPENGER_NÆRING("Sykepenger næring", Kategori.NÆRING, List.of("sykepenger", "sykepengerTilDagmamma", "sykepengerTilFisker", "sykepengerTilJordOgSkogbrukere")),
    OMSORGSPENGER_NÆRING("Omsorgspenger næring", Kategori.NÆRING, List.of("omsorgspenger", "omsorgspengerTilDagmamma", "omsorgspengerTilFisker", "omsorgspengerTilJordOgSkogbrukere")),
    OPPLÆRINGSPENGER_NÆRING("Opplæringspenger næring", Kategori.NÆRING, List.of("opplaeringspenger", "opplaeringspengerTilDagmamma", "opplaeringspengerTilFisker", "opplaeringspengerTilJordOgSkogbrukere")),
    PLEIEPENGER_NÆRING("Pleiepenger næring", Kategori.NÆRING, List.of("pleiepenger", "pleiepengerTilDagmamma", "pleiepengerTilFisker", "pleiepengerTilJordOgSkogbrukere")),
    DAGPENGER_NÆRING("Dagpenger næring", Kategori.NÆRING, List.of("dagpengerVedArbeidsloeshet", "dagpengerTilFisker")),

    // Annen ytelse utbetalt til person som er næringsdrivende
    ANNET("Annet", Kategori.NÆRING, "annet"),
    VEDERLAG("Vederlag", Kategori.NÆRING, List.of("vederlag", "vederlagDagmammaIEgetHjem")),
    LOTT_KUN_TRYGDEAVGIFT("Lott kun trygdeavgift", Kategori.NÆRING, "lottKunTrygdeavgift"),
    KOMPENSASJON_FOR_TAPT_PERSONINNTEKT("Kompensasjon for tapt personinntekt", Kategori.NÆRING, "kompensasjonForTaptPersoninntekt")
    ;

    private final String navn;

    private final Kategori kategori;
    private final List<String> offisiellKode;

    InntektYtelseType(String navn, Kategori kategori, String offisiellKode) {
        this(navn, kategori, List.of(offisiellKode));
    }

    InntektYtelseType(String navn, Kategori kategori, List<String> offisiellKode) {
        this.navn = navn;
        this.kategori = kategori;
        this.offisiellKode = offisiellKode != null ? offisiellKode : List.of();
    }

    public static InntektYtelseType fraKode(String kode) {
        return kode != null ? InntektYtelseType.valueOf(kode) : null;
    }

    public static InntektYtelseType finnForKodeverkEiersKode(Kategori kategori, String kode) {
        return Stream.of(values()).filter(k -> k.kategori == kategori && k.offisiellKode.contains(kode)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Ukjent YtelseInntektType: " + kode + " kategori " + kategori));
    }

    public String getNavn() {
        return navn;
    }

    @Override
    public String getKode() {
        return name();
    }

    @Override
    public String getOffisiellKode() {
        return offisiellKode.stream().findFirst().orElse(null);
    }

    private boolean erOrdinærYtelse() {
        return kategori == Kategori.YTELSE;
    }

    private boolean erNæringsYtelse() {
        return kategori == Kategori.NÆRING;
    }

    public enum Kategori { YTELSE, NÆRING, TRYGD }
}
