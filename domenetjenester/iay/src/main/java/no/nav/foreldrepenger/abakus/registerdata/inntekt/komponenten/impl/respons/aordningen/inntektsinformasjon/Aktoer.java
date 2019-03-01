package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon;

public class Aktoer {
    private String identifikator;
    private AktoerType aktoerType;

    public static Aktoer newOrganisasjon(String identifikator) {
        return new Aktoer(identifikator, AktoerType.ORGANISASJON);
    }

    public static Aktoer newAktoerId(String identifikator) {
        return new Aktoer(identifikator, AktoerType.AKTOER_ID);
    }

    public static Aktoer newNaturligIdent(String identifikator) {
        return new Aktoer(identifikator, AktoerType.NATURLIG_IDENT);
    }

    public String getIdentifikator() {
        return this.identifikator;
    }

    public AktoerType getAktoerType() {
        return this.aktoerType;
    }

    public void setIdentifikator(String identifikator) {
        this.identifikator = identifikator;
    }

    public void setAktoerType(AktoerType aktoerType) {
        this.aktoerType = aktoerType;
    }

    public Aktoer() {
    }

    public Aktoer(String identifikator, AktoerType aktoerType) {
        this.identifikator = identifikator;
        this.aktoerType = aktoerType;
    }

}
