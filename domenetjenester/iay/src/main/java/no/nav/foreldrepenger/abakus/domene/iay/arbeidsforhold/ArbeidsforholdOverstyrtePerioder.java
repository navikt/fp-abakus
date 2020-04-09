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
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;

@Table(name = "IAY_OVERSTYRTE_PERIODER")
@Entity(name = "ArbeidsforholdOverstyrtePerioder")
public class ArbeidsforholdOverstyrtePerioder extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_IAY_OVERSTYRTE_PERIODER")
    private Long id;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fomDato", column = @Column(name = "FOM")),
        @AttributeOverride(name = "tomDato", column = @Column(name = "TOM"))
    })
    private IntervallEntitet periode;

    @JsonBackReference
    @ManyToOne(optional = false)
    @JoinColumn(name = "ARBEIDSFORHOLD_ID", nullable = false, updatable = false)
    private ArbeidsforholdOverstyring arbeidsforholdOverstyring;

    @Version
    @Column(name = "versjon", nullable = false)
    private Long versjon;

    ArbeidsforholdOverstyrtePerioder() {

    }

    ArbeidsforholdOverstyrtePerioder(ArbeidsforholdOverstyrtePerioder arbeidsforholdOverstyrtePerioder) {
        this.periode = arbeidsforholdOverstyrtePerioder.getOverstyrtePeriode();
    }

    void setPeriode(IntervallEntitet periode) {
        this.periode = periode;
    }

    void setArbeidsforholdOverstyring(ArbeidsforholdOverstyring arbeidsforholdOverstyring) {
        this.arbeidsforholdOverstyring = arbeidsforholdOverstyring;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof ArbeidsforholdOverstyrtePerioder)) return false;
        var that = (ArbeidsforholdOverstyrtePerioder) o;
        return Objects.equals(periode, that.periode) && Objects.equals(arbeidsforholdOverstyring, that.arbeidsforholdOverstyring);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, arbeidsforholdOverstyring);
    }

    @Override
    public String toString() {
        return "ArbeidsforholdOverstyrtePerioderEntitet{" +
            "periode=" + periode +
            ", arbeidsforholdOverstyring=" + arbeidsforholdOverstyring +
            '}';
    }

    public IntervallEntitet getOverstyrtePeriode() {
        return periode;
    }
}
