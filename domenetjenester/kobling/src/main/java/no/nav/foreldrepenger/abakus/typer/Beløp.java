package no.nav.foreldrepenger.abakus.typer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.diff.TraverseValue;

/**
 * Beløp representerer kombinasjon av kroner og øre på standardisert format
 */
@Embeddable
public class Beløp implements Serializable, IndexKey, TraverseValue {
    public static final Beløp ZERO = new Beløp(BigDecimal.ZERO);
    private static final RoundingMode AVRUNDINGSMODUS = RoundingMode.HALF_EVEN;

    @Column(name = "beloep", scale = 2)
    @ChangeTracked
    private BigDecimal verdi;

    protected Beløp() {
        // for hibernate
    }

    public Beløp(BigDecimal verdi) {
        this.verdi = verdi;
    }

    private BigDecimal skalertVerdi() {
        return verdi == null ? null : verdi.setScale(2, AVRUNDINGSMODUS);
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {skalertVerdi()};
        return IndexKeyComposer.createKey(keyParts);
    }

    public BigDecimal getVerdi() {
        return verdi;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        Beløp other = (Beløp) obj;
        if (Objects.equals(verdi, other.verdi)) {
            return true;
        }
        if (verdi == null || other.verdi == null) {
            return false;
        }
        return skalertVerdi().compareTo(other.skalertVerdi()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(skalertVerdi());
    }

    @Override
    public String toString() {
        return "Beløp{" + "verdi=" + verdi + ", skalertVerdi=" + skalertVerdi() + '}';
    }

    public int compareTo(Beløp annetBeløp) {
        return verdi.compareTo(annetBeløp.getVerdi());
    }

    public boolean erNulltall() {
        return verdi != null && compareTo(Beløp.ZERO) == 0;
    }
}
