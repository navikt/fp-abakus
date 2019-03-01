package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.impl.respons.aordningen.inntektsinformasjon.inntekt;

public class Loennsinntekt extends Inntekt {
    static final String INNTEKT_TYPE = "LOENNSINNTEKT";
    private Integer antall;

    public Loennsinntekt() {
        super(InntektType.LOENNSINNTEKT);
    }

    public Integer getAntall() {
        return this.antall;
    }

    public void setAntall(Integer antall) {
        this.antall = antall;
    }
}
