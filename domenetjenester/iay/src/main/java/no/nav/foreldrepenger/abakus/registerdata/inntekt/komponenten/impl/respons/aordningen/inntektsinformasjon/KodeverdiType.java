package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon;

public enum KodeverdiType {
    AINNTEKTSFILTER("ainntektsfilter"),
    ARBEIDSFORHOLDSTYPER("arbeidsforholdstyper"),
    ARBEIDSTIDSORDNING("arbeidstidsordning"),
    AVLOENNINGSTYPER("avloenningstyper"),
    EDAGTILLEGGSINFOKATEGORIER("edag.tilleggsinfo.kategorier"),
    FORDEL("fordel"),
    FORMAAL("formaal"),
    FORSKUDDSTREKKSBESKRIVELSE("forskuddstrekksbeskrivelse"),
    FORVENTETINNTEKTHENDELSER("forventet.inntekt.hendelser"),
    FORVENTETINNTEKTINFORMASJONSOPPHAV("forventet.inntekt.informasjonsopphav"),
    FORVENTETINNTEKTTYPER("forventet.inntekt.typer"),
    FRADRAGBESKRIVELSE("fradragbeskrivelse"),
    INFORMASJONSSTATUSER("informasjonsstatuser"),
    INNTEKTSINFORMASJONSOPPHAV("inntekts.informasjonsopphav"),
    INNTEKTSSTATUSER("inntektsstatuser"),
    INNTEKTSPERIODETYPE("inntektsperiodetype"),
    LANDKODER("landkoder"),
    LOENNSBESKRIVELSE("loennsbeskrivelse"),
    NAERINGSINNTEKTSBESKRIVELSE("naeringsinntektsbeskrivelse"),
    PENSJONELLERTRYGDEBESKRIVELSE("pensjon.eller.trygdebeskrivelse"),
    PERSONIDENTER("personidenter"),
    PERSONTYPEFORREISEKOSTLOSJI("persontype.for.reisekostlosji"),
    SPESIELLEINNTJENINGSFORHOLD("spesielle.inntjeningsforhold"),
    YRKER("yrker"),
    YTELSEFRAOFFENTLIGEBESKRIVELSE("ytelse.fra.offentlige.beskrivelse");

    String verdi;

    KodeverdiType(String verdi) {
        this.verdi = verdi;
    }
}
