package no.nav.foreldrepenger.abakus.app.sikkerhet;

public enum AbacResourceAttributt {

    ABAKUS("no.nav.abac.attributter.duplo.abakus"),
    GRUNNLAG("no.nav.abac.attributter.duplo.abakus.grunnlag"),
    ARBEIDSFORHOLD("no.nav.abac.attributter.duplo.abakus.arbeidsforhold"),
    INNTEKSTMELDING("no.nav.abac.attributter.duplo.abakus.inntektsmelding"),
    SØKNAD("no.nav.abac.attributter.duplo.abakus.soeknad"),
    REGISTERDATA("no.nav.abac.attributter.duplo.abakus.registerdata"),
    VEDTAK("no.nav.abac.attributter.duplo.abakus.vedtak"),
    DRIFT("no.nav.abac.attributter.duplo.abakus.drift")
    ;

    private String eksternKode;

    AbacResourceAttributt(String eksternKode) {
        this.eksternKode = eksternKode;
    }

    public String getEksternKode() {
        return eksternKode;
    }
}
