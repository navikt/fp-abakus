package no.nav.foreldrepenger.abakus.app.sikkerhet;

public enum BeskyttetRessursResourceAttributt {

    APPLIKASJON("no.nav.abac.attributter.duplo.abakus"),
    GRUNNLAG("no.nav.abac.attributter.duplo.abakus.grunnlag"),
    REGISTERDATA("no.nav.abac.attributter.duplo.abakus.registerdata"),
    DRIFT("no.nav.abac.attributter.duplo.abakus.drift"),
    /**
     * Skal kun brukes av Interceptor
     */
    DUMMY(null);

    private String eksternKode;

    BeskyttetRessursResourceAttributt(String eksternKode) {
        this.eksternKode = eksternKode;
    }

    public String getEksternKode() {
        return eksternKode;
    }
}
