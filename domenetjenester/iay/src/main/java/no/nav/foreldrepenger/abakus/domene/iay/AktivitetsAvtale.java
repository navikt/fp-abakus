package no.nav.foreldrepenger.abakus.domene.iay;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface AktivitetsAvtale {

    /**
     * Avtalt prosentsats i avtalen
     *
     * @return prosent
     */
    Stillingsprosent getProsentsats();

    /**
     * Periode start
     *
     * @return Første dag i perioden
     */
    LocalDate getFraOgMed();

    boolean erOverstyrtPeriode();

    /**
     * Periode slutt
     *
     * @return siste dag i perioden
     */
    LocalDate getTilOgMed();

    /**
     * Periode
     *
     * @return hele perioden
     */
    DatoIntervallEntitet getPeriode();

    /**
     * Siste lønnsendingsdato
     *
     * @return hele perioden
     */
    LocalDate getSisteLønnsendringsdato();

    boolean matcherPeriode(DatoIntervallEntitet aktivitetsAvtale);

    /**
     * Er avtallen løpende
     *
     * @return true/false
     */
    boolean getErLøpende();

    String getBeskrivelse();

    Yrkesaktivitet getYrkesaktivitet();

    boolean erAnsettelsesPeriode();

    /** Returner {@link #getProsentsats()} (skalert) eller null.*/
    BigDecimal getProsentsatsVerdi();
}
