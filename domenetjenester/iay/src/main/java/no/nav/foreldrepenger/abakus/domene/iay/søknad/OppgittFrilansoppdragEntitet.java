package no.nav.foreldrepenger.abakus.domene.iay.søknad;

import java.util.Objects;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittFrilansoppdrag;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;


@Table(name = "IAY_OPPGITT_FRILANSOPPDRAG")
@Entity(name = "Frilansoppdrag")
public class OppgittFrilansoppdragEntitet extends BaseEntitet implements OppgittFrilansoppdrag, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SO_OPPGITT_FRILANSOPPDRAG")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "frilans_id", nullable = false, updatable = false)
    private OppgittFrilansEntitet frilans;

    @ChangeTracked
    private String oppdragsgiver;

    @Embedded
    @ChangeTracked
    private DatoIntervallEntitet periode;


    OppgittFrilansoppdragEntitet() {
    }

    public OppgittFrilansoppdragEntitet(String oppdragsgiver, DatoIntervallEntitet periode) {
        this.oppdragsgiver = oppdragsgiver;
        this.periode = periode;
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(periode, oppdragsgiver);
    }

    public void setOppgittOpptjening(OppgittFrilansEntitet frilans) {
        this.frilans = frilans;
    }

    void setPeriode(DatoIntervallEntitet periode) {
        this.periode = periode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof OppgittFrilansoppdragEntitet)) return false;
        OppgittFrilansoppdragEntitet that = (OppgittFrilansoppdragEntitet) o;
        return Objects.equals(frilans, that.frilans) &&
            Objects.equals(oppdragsgiver, that.oppdragsgiver) &&
            Objects.equals(periode, that.periode);
    }
    

    @Override
    public int hashCode() {
        return Objects.hash(frilans, oppdragsgiver, periode);
    }

    @Override
    public String toString() {
        return "FrilansoppdragEntitet{" +
            "frilans=" + frilans +
            ", oppdragsgiver='" + oppdragsgiver + '\'' +
            ", periode=" + periode +
            '}';
    }

    @Override
    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    @Override
    public String getOppdragsgiver() {
        return oppdragsgiver;
    }

    public void setFrilans(OppgittFrilansEntitet frilans) {
        this.frilans = frilans;
    }
}
