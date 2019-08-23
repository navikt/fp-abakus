package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Collection;

import no.nav.foreldrepenger.abakus.typer.AktørId;

public interface AktørInntekt {

    /**
     * Aktøren inntekten er relevant for
     *
     * @return aktørid
     */
    AktørId getAktørId();

    /** Returner alle inntekter, ufiltrert. */
    Collection<Inntekt> getInntekt();
}
