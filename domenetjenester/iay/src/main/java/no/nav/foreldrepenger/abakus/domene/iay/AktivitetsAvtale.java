package no.nav.foreldrepenger.abakus.domene.iay;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.foreldrepenger.abakus.typer.AntallTimer;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public interface AktivitetsAvtale {


    /**
     * For timelønnede så vil antallet timer i arbeidsavtalen være satt her
     * @return antall timer
     *
     */
    AntallTimer getAntallTimer();

    /**
     * Antall timer som tilsvarer fulltid (f.eks 40 timer)
     * @return antall timer
     */
    AntallTimer getAntallTimerFulltid();

    /**
     * Avtalt prosentsats i avtalen
     *
     * @return prosent
     */
    Stillingsprosent getProsentsats();

    /**
     * Hvorvidt aktivitetsavtalen har en overstyrt periode eller ikke.
     *
     * @return boolean, true hvis overstyrt, false hvis ikke.
     */
    boolean erOverstyrtPeriode();

    /**
     * Periode
     *
     * @return hele perioden
     */
    DatoIntervallEntitet getPeriode();

    /**
     * Perioden til aktivitetsavtalen.
     * Tar Ikke hensyn til overstyring gjort i 5080.
     *
     * @return Hele perioden, tar Ikke hensyn til overstyringer.
     */
    DatoIntervallEntitet getPeriodeUtenOverstyring();

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

    boolean erAnsettelsesPeriode();

    /** Returner {@link #getProsentsats()} (skalert) eller null.*/
    BigDecimal getProsentsatsVerdi();
}
