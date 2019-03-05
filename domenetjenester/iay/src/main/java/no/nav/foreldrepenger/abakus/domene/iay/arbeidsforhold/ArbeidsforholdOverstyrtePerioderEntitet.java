package no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold;

import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Table(name = "IAY_OVERSTYRTE_PERIODER")
@Entity(name = "ArbeidsforholdOverstyrtePerioder")
public class ArbeidsforholdOverstyrtePerioderEntitet extends BaseEntitet implements ArbeidsforholdOverstyrtePerioder {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_IAY_OVERSTYRTE_PERIODER")
    private Long id;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fomDato", column = @Column(name = "FOM")),
        @AttributeOverride(name = "tomDato", column = @Column(name = "TOM"))
    })
    private DatoIntervallEntitet periode;

    @JsonBackReference
    @ManyToOne(optional = false)
    @JoinColumn(name = "ARBEIDSFORHOLD_ID", nullable = false, updatable = false)
    private ArbeidsforholdOverstyringEntitet arbeidsforholdOverstyring;

    @Version
    @Column(name = "versjon", nullable = false)
    private Long versjon;

    ArbeidsforholdOverstyrtePerioderEntitet() {

    }

    void setPeriode(DatoIntervallEntitet periode) {
        this.periode = periode;
    }

    public ArbeidsforholdOverstyringEntitet getArbeidsforholdOverstyring() {
        return arbeidsforholdOverstyring;
    }

    void setArbeidsforholdOverstyring(ArbeidsforholdOverstyringEntitet arbeidsforholdOverstyring) {
        this.arbeidsforholdOverstyring = arbeidsforholdOverstyring;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArbeidsforholdOverstyrtePerioderEntitet that = (ArbeidsforholdOverstyrtePerioderEntitet) o;
        return Objects.equals(periode, that.periode) && Objects.equals(arbeidsforholdOverstyring, that.arbeidsforholdOverstyring);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, arbeidsforholdOverstyring);
    }

    @Override
    public String toString() {
        return "ArbeidsforholdInformasjonEntitet{" +
            "periode=" + periode +
            ", arbeidsforholdOverstyring=" + arbeidsforholdOverstyring +
            '}';
    }

    @Override
    public DatoIntervallEntitet getOverstyrtePerioder() {
        return periode;
    }
}
