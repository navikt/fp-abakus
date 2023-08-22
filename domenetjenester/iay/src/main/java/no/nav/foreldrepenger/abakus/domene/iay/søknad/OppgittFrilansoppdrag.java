package no.nav.foreldrepenger.abakus.domene.iay.søknad;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;


@Table(name = "IAY_OPPGITT_FRILANSOPPDRAG")
@Entity(name = "Frilansoppdrag")
public class OppgittFrilansoppdrag extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SO_OPPGITT_FRILANSOPPDRAG")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "frilans_id", nullable = false, updatable = false)
    private OppgittFrilans frilans;

    @ChangeTracked
    private String oppdragsgiver;

    @Embedded
    @ChangeTracked
    private IntervallEntitet periode;

    @Column(name = "inntekt")
    private BigDecimal inntekt;

    OppgittFrilansoppdrag() {
    }

    public OppgittFrilansoppdrag(String oppdragsgiver, IntervallEntitet periode, BigDecimal inntekt) {
        this.oppdragsgiver = oppdragsgiver;
        this.periode = periode;
        this.inntekt = inntekt;
    }

    /* copy ctor */
    public OppgittFrilansoppdrag(OppgittFrilansoppdrag orginal) {
        oppdragsgiver = orginal.getOppdragsgiver();
        periode = orginal.getPeriode();
        inntekt = orginal.getInntekt();
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {periode, oppdragsgiver};
        return IndexKeyComposer.createKey(keyParts);
    }

    public void setOppgittOpptjening(OppgittFrilans frilans) {
        this.frilans = frilans;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof OppgittFrilansoppdrag)) {
            return false;
        }
        OppgittFrilansoppdrag that = (OppgittFrilansoppdrag) o;
        return Objects.equals(frilans, that.frilans) && Objects.equals(oppdragsgiver, that.oppdragsgiver) && Objects.equals(periode, that.periode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(frilans, oppdragsgiver, periode);
    }

    @Override
    public String toString() {
        return "OppgittFrilansoppdrag{" + "frilans=" + frilans + ", oppdragsgiver='" + oppdragsgiver + '\'' + ", periode=" + periode + '}';
    }

    public IntervallEntitet getPeriode() {
        return periode;
    }

    void setPeriode(IntervallEntitet periode) {
        this.periode = periode;
    }

    public String getOppdragsgiver() {
        return oppdragsgiver;
    }

    public void setFrilans(OppgittFrilans frilans) {
        this.frilans = frilans;
    }

    public BigDecimal getInntekt() {
        return inntekt;
    }
}
