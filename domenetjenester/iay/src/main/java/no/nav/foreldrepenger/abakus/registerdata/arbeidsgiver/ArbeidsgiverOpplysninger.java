package no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver;

public class ArbeidsgiverOpplysninger {

    private final String identifikator;
    private final String navn;

    public ArbeidsgiverOpplysninger(String identifikator, String navn) {
        this.identifikator = identifikator;
        this.navn = navn;
    }

    public String getIdentifikator() {
        return identifikator;
    }

    public String getNavn() {
        return navn;
    }
}
