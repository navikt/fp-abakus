package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Collection;

import no.nav.foreldrepenger.abakus.typer.AktørId;

public interface AktørYtelse {

    /**
     * Aktøren tilstøtende ytelser gjelder for
     *
     * @return aktørId
     */
    AktørId getAktørId();

    /**
     * Tilstøtende ytelser
     *
     * @return liste av {@link Ytelse}
     */
    Collection<Ytelse> getYtelser();

    Long getId();
}
