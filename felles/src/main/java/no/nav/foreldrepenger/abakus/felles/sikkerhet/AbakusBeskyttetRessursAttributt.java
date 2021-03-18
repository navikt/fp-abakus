package no.nav.foreldrepenger.abakus.felles.sikkerhet;

import java.util.Set;

public final class AbakusBeskyttetRessursAttributt {
    public static final String ABAKUS = "no.nav.abac.attributter.resource.duplo.abakus";
    public static final String GRUNNLAG = "no.nav.abac.attributter.resource.duplo.abakus.grunnlag";
    public static final String ARBEIDSFORHOLD = "no.nav.abac.attributter.resource.duplo.abakus.arbeidsforhold";
    public static final String INNTEKSTMELDING = "no.nav.abac.attributter.resource.duplo.abakus.inntektsmelding";
    public static final String SØKNAD = "no.nav.abac.attributter.resource.duplo.abakus.soeknad";
    public static final String REGISTERDATA = "no.nav.abac.attributter.resource.duplo.abakus.registerdata";
    public static final String VEDTAK = "no.nav.abac.attributter.resource.duplo.abakus.vedtak";
    public static final String DRIFT = "no.nav.abac.attributter.resource.duplo.abakus.drift";

    public static final Set<String> ALLE_GODKJENTE_KODER =
            Set.of(ABAKUS, GRUNNLAG, ARBEIDSFORHOLD, INNTEKSTMELDING, SØKNAD, REGISTERDATA, VEDTAK, DRIFT);
}
