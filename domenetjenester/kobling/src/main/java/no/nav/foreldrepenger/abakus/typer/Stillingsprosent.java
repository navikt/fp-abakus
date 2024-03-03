package no.nav.foreldrepenger.abakus.typer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.diff.TraverseValue;

/**
 * Stillingsprosent slik det er oppgitt i arbeidsavtalen
 */
@Embeddable
public class Stillingsprosent implements Serializable, IndexKey, TraverseValue {
    private static final Logger LOG = LoggerFactory.getLogger(Stillingsprosent.class);

    private static final RoundingMode AVRUNDINGSMODUS = RoundingMode.HALF_EVEN;

    private static final BigDecimal MAX_VERDI = BigDecimal.valueOf(109.99d); // Bør være 100 men vil dobbelsjekke avrunding i noen tilfelle

    private static final Stillingsprosent NULL_PROSENT = new Stillingsprosent(null);

    @Column(name = "verdi", scale = 2, nullable = false)
    @ChangeTracked
    private BigDecimal verdi;

    protected Stillingsprosent() {
        // for hibernate
    }

    public Stillingsprosent(BigDecimal verdi) {
        this.verdi = fiksNegativOgMax(verdi);
        validerRange(this.verdi);
    }

    public static Stillingsprosent nullProsent() {
        return NULL_PROSENT;
    }

    private static void validerRange(BigDecimal verdi) {
        if (verdi == null) {
            return;
        }
        if (BigDecimal.ZERO.compareTo(verdi) > 0) {
            throw new IllegalArgumentException("Prosent må være >= 0");
        }
    }

    public BigDecimal getVerdi() {
        return verdi;
    }

    public boolean erNulltall() {
        return verdi != null && verdi.intValue() == 0;
    }

    private BigDecimal skalertVerdi() {
        return verdi == null ? null : verdi.setScale(2, AVRUNDINGSMODUS);
    }

    private BigDecimal fiksNegativOgMax(BigDecimal verdi) {
        if (verdi == null) {
            return null;
        }
        if (verdi.compareTo(BigDecimal.ZERO) < 0) {
            LOG.info("[IAY] Prosent (yrkesaktivitet, permisjon) mindre enn 0, absolutt verdi brukes isteden. Verdi fra AA-reg: {}", verdi);
            verdi = verdi.abs();
        }
        while (verdi.compareTo(MAX_VERDI) > 0) {
            LOG.info("[IAY] Prosent (yrkesaktivitet, permisjon) mer enn 109,99, justert verdi brukes isteden. Verdi fra AA-reg: {}", verdi);
            verdi = verdi.divide(BigDecimal.TEN, 2, AVRUNDINGSMODUS);
        }
        return verdi;
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {skalertVerdi()};
        return IndexKeyComposer.createKey(keyParts);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        Stillingsprosent other = (Stillingsprosent) obj;
        return Objects.equals(skalertVerdi(), other.skalertVerdi());
    }

    @Override
    public int hashCode() {
        return Objects.hash(skalertVerdi());
    }

    @Override
    public String toString() {
        return "Stillingsprosent{" + "verdi=" + verdi + ", skalertVerdi=" + skalertVerdi() + '}';
    }
}
