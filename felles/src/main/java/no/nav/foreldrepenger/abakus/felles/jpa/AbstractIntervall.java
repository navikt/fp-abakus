package no.nav.foreldrepenger.abakus.felles.jpa;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import no.nav.vedtak.konfig.Tid;

public abstract class AbstractIntervall implements Comparable<AbstractIntervall>, Serializable {

    public static final LocalDate TIDENES_BEGYNNELSE = Tid.TIDENES_BEGYNNELSE;
    public static final LocalDate TIDENES_ENDE = Tid.TIDENES_ENDE;

    static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public abstract LocalDate getFomDato();

    public abstract LocalDate getTomDato();


    public boolean overlapper(AbstractIntervall other) {
        boolean fomBeforeOrEqual = erEtterEllerLikPeriodestart(other.getTomDato());
        boolean tomAfterOrEqual = erFørEllerLikPeriodeslutt(other.getFomDato());
        return fomBeforeOrEqual && tomAfterOrEqual;
    }

    public boolean inkluderer(LocalDate dato) {
        return erEtterEllerLikPeriodestart(dato) && erFørEllerLikPeriodeslutt(dato);
    }

    private boolean erEtterEllerLikPeriodestart(LocalDate dato) {
        return getFomDato().isBefore(dato) || getFomDato().isEqual(dato);
    }

    private boolean erFørEllerLikPeriodeslutt(LocalDate dato) {
        return getTomDato() == null || getTomDato().isAfter(dato) || getTomDato().isEqual(dato);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof AbstractIntervall)) {
            return false;
        }
        AbstractIntervall annen = (AbstractIntervall) object;
        return this.getFomDato().equals(annen.getFomDato()) && this.getTomDato().equals(annen.getTomDato());
    }

    @Override
    public int compareTo(AbstractIntervall periode) {
        return getFomDato().compareTo(periode.getFomDato());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFomDato(), getTomDato());
    }

    @Override
    public String toString() {
        return String.format("Periode: %s - %s", getFomDato().format(FORMATTER), getTomDato().format(FORMATTER));
    }
}
