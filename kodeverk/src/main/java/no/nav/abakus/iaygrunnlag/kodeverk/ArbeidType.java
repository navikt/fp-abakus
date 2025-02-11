package no.nav.abakus.iaygrunnlag.kodeverk;
/**
 * Typer av arbeidsforhold.
 *
 * <p>
 *
 * <h3>Kilde: NAV kodeverk</h3>
 *
 * https://modapp.adeo.no/kodeverksklient/viskodeverk/Arbeidsforholdstyper/2
 *
 * <p>
 *
 * <h3>Tjeneste(r) som returnerer dette:</h3>
 *
 * <ul>
 *   <li>https://confluence.adeo.no/display/SDFS/tjeneste_v3%3Avirksomhet%3AArbeidsforhold_v3
 * </ul>
 *
 * <h3>Tjeneste(r) som konsumerer dete:</h3>
 *
 * <ul>
 *   <li>
 * </ul>
 */
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@JsonAutoDetect(
        getterVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE,
        fieldVisibility = Visibility.ANY)
public enum ArbeidType implements Kodeverdi {
    ETTERLØNN_SLUTTPAKKE("ETTERLØNN_SLUTTPAKKE", "Etterlønn eller sluttpakke", null, true),
    FORENKLET_OPPGJØRSORDNING(
            "FORENKLET_OPPGJØRSORDNING", "Forenklet oppgjørsordning ", "forenkletOppgjoersordning", false),
    FRILANSER("FRILANSER", "Frilanser, samlet aktivitet", null, true),
    FRILANSER_OPPDRAGSTAKER_MED_MER(
            "FRILANSER_OPPDRAGSTAKER",
            "Frilansere/oppdragstakere, med mer",
            "frilanserOppdragstakerHonorarPersonerMm",
            false),
    LØNN_UNDER_UTDANNING("LØNN_UNDER_UTDANNING", "Lønn under utdanning", null, true),
    MARITIMT_ARBEIDSFORHOLD("MARITIMT_ARBEIDSFORHOLD", "Maritimt arbeidsforhold", "maritimtArbeidsforhold", false),
    MILITÆR_ELLER_SIVILTJENESTE("MILITÆR_ELLER_SIVILTJENESTE", "Militær eller siviltjeneste", null, true),
    ORDINÆRT_ARBEIDSFORHOLD("ORDINÆRT_ARBEIDSFORHOLD", "Ordinært arbeidsforhold", "ordinaertArbeidsforhold", false),
    PENSJON_OG_ANDRE_TYPER_YTELSER_UTEN_ANSETTELSESFORHOLD(
            "PENSJON_OG_ANDRE_TYPER_YTELSER_UTEN_ANSETTELSESFORHOLD",
            "Pensjoner og andre typer ytelser",
            "pensjonOgAndreTyperYtelserUtenAnsettelsesforhold",
            false),
    SELVSTENDIG_NÆRINGSDRIVENDE("NÆRING", "Selvstendig næringsdrivende", null, false),
    UTENLANDSK_ARBEIDSFORHOLD("UTENLANDSK_ARBEIDSFORHOLD", "Arbeid i utlandet", null, true),
    VENTELØNN_VARTPENGER("VENTELØNN_VARTPENGER", "Ventelønn eller vartpenger", null, true),
    VANLIG("VANLIG", "Vanlig", "VANLIG", false),
    UDEFINERT("-", "Ikke definert", null, false),
    ;

    private static final Set<ArbeidType> AA_REGISTER_TYPER = Set.of(
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD,
            ArbeidType.MARITIMT_ARBEIDSFORHOLD,
            ArbeidType.FORENKLET_OPPGJØRSORDNING,
            ArbeidType.FRILANSER_OPPDRAGSTAKER_MED_MER);
    private static final Set<ArbeidType> AA_REGISTER_ARBEID_TYPER = Set.of(
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD,
            ArbeidType.MARITIMT_ARBEIDSFORHOLD,
            ArbeidType.FORENKLET_OPPGJØRSORDNING);
    private static final Map<String, ArbeidType> KODER = new LinkedHashMap<>();
    private static final Map<String, ArbeidType> OFFISIELLE_KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
            if (v.getOffisiellKode() != null) {
                OFFISIELLE_KODER.put(v.getOffisiellKode(), v);
            }
        }
    }

    @JsonValue
    private final String kode;

    private final String navn;

    private final String offisiellKode;

    private final boolean visGui;

    ArbeidType(String kode, String navn, String offisiellKode, boolean visGui) {
        this.kode = kode;
        this.navn = navn;
        this.visGui = visGui;
        this.offisiellKode = offisiellKode;
    }

    public static ArbeidType finnForKodeverkEiersKode(String offisiellKode) {
        return offisiellKode != null ? OFFISIELLE_KODER.getOrDefault(offisiellKode, UDEFINERT) : UDEFINERT;
    }

    public static ArbeidType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode))
                .orElseThrow(() -> new IllegalArgumentException("Ukjent ArbeidType: " + kode));
    }

    public static Map<String, ArbeidType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public static boolean erRegisterType(ArbeidType type) {
        return AA_REGISTER_TYPER.contains(type);
    }

    public static boolean erRegisterArbeid(ArbeidType type) {
        return AA_REGISTER_ARBEID_TYPER.contains(type);
    }

    public boolean erAnnenOpptjening() {
        return visGui;
    }

    @Override
    public String getKode() {
        return kode;
    }

    public String getNavn() {
        return navn;
    }

    @Override
    public String getOffisiellKode() {
        return offisiellKode;
    }
}
