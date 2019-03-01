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
     * Collection av aktiviteter filtrert iht ArbeidsforholdInformasjon, uten frilans enkeltoppdrag  {@link no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType}
     *
     * @return Liste av {@link Yrkesaktivitet}
     */
    Collection<Yrkesaktivitet> getYrkesaktiviteter();

    /**
     * Collection av frilansaktiviteter / enkeltoppdrag {@link no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType}
     *
     * @return Liste av {@link Yrkesaktivitet}
     */
    Collection<Yrkesaktivitet> getFrilansOppdrag();
}
