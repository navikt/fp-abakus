package no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.Objects;
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

    @ManyToOne(optional = false)
    @JoinColumn(name = "ARBEIDSFORHOLD_ID", nullable = false, updatable = false)
    private ArbeidsforholdOverstyring arbeidsforholdOverstyring;

    @Version
    @Column(name = "versjon", nullable = false)
    private Long versjon;

    ArbeidsforholdOverstyrtePerioder() {}

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
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof ArbeidsforholdOverstyrtePerioder)) {
            return false;
        }
        var that = (ArbeidsforholdOverstyrtePerioder) o;
        return Objects.equals(periode, that.periode)
                && Objects.equals(arbeidsforholdOverstyring, that.arbeidsforholdOverstyring);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, arbeidsforholdOverstyring);
    }

    @Override
    public String toString() {
        return "ArbeidsforholdOverstyrtePerioderEntitet{" + "periode=" + periode + ", arbeidsforholdOverstyring="
                + arbeidsforholdOverstyring + '}';
    }

    public IntervallEntitet getOverstyrtePeriode() {
        return periode;
    }
}
