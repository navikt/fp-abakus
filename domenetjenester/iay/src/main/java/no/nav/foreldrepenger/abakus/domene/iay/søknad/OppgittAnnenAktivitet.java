package no.nav.foreldrepenger.abakus.domene.iay.søknad;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidType;
import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.iay.jpa.ArbeidTypeKodeverdiConverter;


@Table(name = "IAY_ANNEN_AKTIVITET")
@Entity(name = "AnnenAktivitet")
public class OppgittAnnenAktivitet extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_ANNEN_AKTIVITET")
    private Long id;

    @Embedded
    @ChangeTracked
    private IntervallEntitet periode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "oppgitt_opptjening_id", nullable = false, updatable = false)
    private OppgittOpptjening oppgittOpptjening;

    @ChangeTracked
    @Convert(converter = ArbeidTypeKodeverdiConverter.class)
    @Column(name = "arbeid_type", nullable = false, updatable = false)
    private ArbeidType arbeidType;

    public OppgittAnnenAktivitet(IntervallEntitet periode, ArbeidType arbeidType) {
        this.periode = periode;
        this.arbeidType = arbeidType;
    }

    public OppgittAnnenAktivitet() {
        // hibernate
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = { periode, arbeidType };
        return IndexKeyComposer.createKey(keyParts);
    }

    public ArbeidType getArbeidType() {
        return arbeidType;
    }

    public IntervallEntitet getPeriode() {
        return periode;
    }

    public void setOppgittOpptjening(OppgittOpptjening oppgittOpptjening) {
        this.oppgittOpptjening = oppgittOpptjening;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof OppgittAnnenAktivitet)) return false;
        var that = (OppgittAnnenAktivitet) o;
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
