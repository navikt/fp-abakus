package no.nav.foreldrepenger.abakus.registerdata.infotrygd;

/** Nødnummer for infotrygd. Brukt i saker med delvis refusjon. Kun til sjekk mot orgnummer. */
public enum Nødnummer {
    NØDNUMMER_FOR_TRYGDEETATEN("973626108"),
    NØDNUMMER_FOR_TRYGDEETATEN_2("973626116"),
    NØDNUMMER_FOR_TRYGDEETATEN_3("971278420"),
    NØDNUMMER_FOR_TRYGDEETATEN_4("971278439"),
    NØDNUMMER_FOR_TRYGDEETATEN_5("971248106"),
    NØDNUMMER_FOR_TRYGDEETATEN_6("971373032"),
    NØDNUMMER_FOR_TRYGDEETATEN_FISKER_MED_HYRE("871400172"),
    NØDNUMMER_FREDRIKSTAD_TRYGDEKONTOR("973695061"),
    NØDNUMMER_TROMSØ_TRYGDEKONTOR("973540017");

    private final String orgnummer;

    Nødnummer(String orgnummer) {
        this.orgnummer = orgnummer;
    }

    public String getOrgnummer() {
        return orgnummer;
    }
}
