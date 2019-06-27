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

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittAnnenAktivitet;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;


@Table(name = "IAY_ANNEN_AKTIVITET")
@Entity(name = "AnnenAktivitet")
public class OppgittAnnenAktivitetEntitet extends BaseEntitet implements OppgittAnnenAktivitet, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_ANNEN_AKTIVITET")
    private Long id;

    @Embedded
    @ChangeTracked
    private DatoIntervallEntitet periode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "oppgitt_opptjening_id", nullable = false, updatable = false)
    private OppgittOpptjeningEntitet oppgittOpptjening;

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "arbeid_type", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + ArbeidType.DISCRIMINATOR + "'"))})
    @ChangeTracked
    private ArbeidType arbeidType;

    public OppgittAnnenAktivitetEntitet(DatoIntervallEntitet periode, ArbeidType arbeidType) {
        this.periode = periode;
        this.arbeidType = arbeidType;
    }

    public OppgittAnnenAktivitetEntitet() {
        // hibernate
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(periode, arbeidType);
    }

    @Override
    public ArbeidType getArbeidType() {
        return arbeidType;
    }

    @Override
    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    public void setOppgittOpptjening(OppgittOpptjeningEntitet oppgittOpptjening) {
        this.oppgittOpptjening = oppgittOpptjening;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof OppgittAnnenAktivitetEntitet)) return false;
        var that = (OppgittAnnenAktivitetEntitet) o;
        return Objects.equals(periode, that.periode) &&
            Objects.equals(arbeidType, that.arbeidType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, arbeidType);
    }

    @Override
    public String toString() {
        return "AnnenAktivitetEntitet{" +
            "id=" + id +
            ", periode=" + periode +
            ", arbeidType=" + arbeidType +
            '}';
    }
}
