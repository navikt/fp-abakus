package no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.person.domene;

public class Statsborgerskap {

    private String landkode;

    public Statsborgerskap(String landkode){
        this.landkode = landkode;
    }

    public String getLandkode() {
        return landkode;
    }

    public void setLandkode(String landkode) {
        this.landkode = landkode;
    }
}
