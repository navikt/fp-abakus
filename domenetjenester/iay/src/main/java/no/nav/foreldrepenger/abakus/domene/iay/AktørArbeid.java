package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Collection;

import no.nav.foreldrepenger.abakus.typer.AktørId;

public interface AktørArbeid {

    /**
     * Aktøren som avtalene gjelder for
     *
     * @return aktørId
     */
    AktørId getAktørId();

    /** 
     * Alle yrkesaktiviteter (ufiltret ifht skjæringstidspunkt vurdering. )
     */
    Collection<Yrkesaktivitet> hentAlleYrkesaktiviteter();
}
