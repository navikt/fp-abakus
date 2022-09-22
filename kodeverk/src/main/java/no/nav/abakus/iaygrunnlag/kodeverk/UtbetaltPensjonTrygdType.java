package no.nav.abakus.iaygrunnlag.kodeverk;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum UtbetaltPensjonTrygdType implements UtbetaltYtelseType {

    ALDERSPENSJON("ALDERSPENSJON", "Alderspensjon", "alderspensjon"),
    ALDERSPENSJON_SKJERMINGSTILLEGG("ALDERSPENSJON_SKJERMINGSTILLEGG", "Alderspensjon skjermingstillegg", "alderspensjonSkjermingstillegg"),
    ANNET("ANNET", "Annet", "annet"),
    AFP("AFP", "Avtalefestet pensjon", "avtalefestetPensjon"),
    BARNEPENSJON("BARNEPENSJON", "Barnepensjon", "barnepensjon"),
    BARNEPENSJON_ANDRE("BARNEPENSJON_ANDRE", "Barnepensjon fra andre enn folketrygden", "barnepensjonFraAndreEnnFolketrygden"),
    BIL("BIL", "Bil", "bil"),
    BOLIG("BOLIG", "Bolig", "bolig"),
    EKTEFELLE("EKTEFELLE", "Ektefelletillegg", "ektefelletillegg"),
    ELEKTRONISK_KOMMUNIKASJON("ELEKTRONISK_KOMMUNIKASJON", "Elektronisk kommunikasjon", "elektroniskKommunikasjon"),
    INNSKUDDS_ENGANGS("INNSKUDDS_ENGANGS", "Engangsutbetaling innskuddspensjon", "engangsutbetalingInnskuddspensjon"),
    ETTERLATTE_PENSJON("ETTERLATTE_PENSJON", "Etterlatte pensjon", "etterlattepensjon"),
    ETTERLØNN("ETTERLØNN", "Etterlønn", "etterloenn"),
    ETTERLØNN_OG_ETTERPENSJON("ETTERLØNN_OG_ETTERPENSJON", "Etterlønn og etterpensjon", "etterloennOgEtterpensjon"),
    FØDERÅD("FØDERÅD", "Føderåd", "foederaad"),
    INTRODUKSJONSSTØNAD("INTRODUKSJONSSTØNAD", "Introduksjonsstønad", "introduksjonsstoenad"),
    IPA_IPS_BARNEPENSJON("IPA_IPS_BARNEPENSJON", "Ipa eller ips barnepensjon", "ipaEllerIpsBarnepensjon"),
    IPA_IPS_ENGANGSUTBETALING("IPA_IPS_ENGANGSUTBETALING", "Ipa eller ips engangsutbetaling", "ipaEllerIpsEngangsutbetaling"),
    IPA_IPS_PERIODISKE("IPA_IPS_PERIODISKE", "Ipa eller ips periodiske ytelser", "ipaEllerIpsPeriodiskeYtelser"),
    IPA_IPS_UFØRE("IPA_IPS_UFØRE", "Ipa eller ips uførepensjon", "ipaEllerIpsUfoerepensjon"),
    KRIGSPENSJON("KRIGSPENSJON", "Krigspensjon", "krigspensjon"),
    KVALIFISERINGSSTØNAD("KVALIFISERINGSSTØNAD", "Kvalifiseringsstønad", "kvalifiseringstoenad"),
    NY_AFP("NY_AFP", "Ny avtalefestet pensjon privat sektor", "nyAvtalefestetPensjonPrivatSektor"),
    NYE_LIVRENTER("NYE_LIVRENTER", "Nye livrenter i arbeidsforhold og livrenter fortsettelsesforsikringer", "nyeLivrenterIArbeidsforholdOgLivrenterFortsettelsesforsikringer"),
    OVERGANGSSTØNAD_ENSLIG("OVERGANGSSTØNAD_ENSLIG", "Overgangsstønad til enslig mor eller far", "overgangsstoenadTilEnsligMorEllerFarSomBegynteAaLoepe31Mars2014EllerTidligere"),
    OVERGANGSSTØNAD_EKTEFELLE("OVERGANGSSTØNAD_EKTEFELLE", "Overgangsstønad til gjenlevende ektefelle", "overgangsstoenadTilGjenlevendeEktefelle"),
    PENSJON_DØDSMÅNED("PENSJON_DØDSMÅNED", "Pensjon i dødsmåned", "pensjonIDoedsmaaneden"),
    LIVRENTER("LIVRENTER", "Pensjon og livrenter i arbeidsforhold", "pensjonOgLivrenterIArbeidsforhold"),
    RENTEFORDEL_LÅN("RENTEFORDEL_LÅN", "Rentefordel lån", "rentefordelLaan"),
    SUPPLERENDE_STØNAD("SUPPLERENDE_STØNAD", "Supplerende stønad til person med kort botid i Norge", "supplerendeStoenadTilPersonKortBotidNorge"),
    SUPPLERENDE_FLYKTNING("SUPPLERENDE_FLYKTNING", "Supplerende stønad til ufør flyktning", "supplerendeStoenadTilUfoerFlyktning"),
    SLITERTILLEGG("SLITERTILLEGG", "Slitertillegg", "slitertillegg"),
    UFØREPENSJON("UFØREPENSJON", "Uførepensjon", "ufoerepensjon"),
    UFØREPENSJON_ANDRE("UFØREPENSJON_ANDRE", "Uførepensjon fra andre enn folketrygden", "ufoerepensjonFraAndreEnnFolketrygden"),
    UFØREPENSJON_ANDRE_ETTEROPPGJØR("UFØREPENSJON_ANDRE_ETTEROPPGJØR", "Uførepensjon etteroppgjør fra andre enn folketrygden", "ufoereytelseEtteroppgjoerFraAndreEnnFolketrygden"),
    UNDERHOLDNINGSBIDRAG("UNDERHOLDNINGSBIDRAG", "Underholdningsbidrag tidligere ektefelle", "underholdsbidragTilTidligereEktefelle"),

    UDEFINERT("-", "Undefined", null),
    ;

    private static final Map<String, UtbetaltPensjonTrygdType> KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "PENSJON_TRYGD_BESKRIVELSE";

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private String navn;

    @JsonValue
    private String kode;

    private String offisiellKode;

    private UtbetaltPensjonTrygdType(String kode) {
        this.kode = kode;
    }

    private UtbetaltPensjonTrygdType(String kode, String navn, String offisiellKode) {
        this.kode = kode;
        this.navn = navn;
        this.offisiellKode = offisiellKode;
    }

    public static UtbetaltPensjonTrygdType fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode))
            .orElseThrow(() -> new IllegalArgumentException("Ukjent PensjonTrygdType: " + kode));
    }

    public static Map<String, UtbetaltPensjonTrygdType> kodeMap() {
        return Collections.unmodifiableMap(KODER);
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
        return offisiellKode;
    }

    public static UtbetaltPensjonTrygdType finnForKodeverkEiersKode(String offisiellDokumentType) {
        return List.of(values()).stream().filter(k -> Objects.equals(k.offisiellKode, offisiellDokumentType)).findFirst().orElse(UDEFINERT);
    }


    @Override
    public boolean erUdefinert() {
        return UDEFINERT.equals(this);
    }
}
