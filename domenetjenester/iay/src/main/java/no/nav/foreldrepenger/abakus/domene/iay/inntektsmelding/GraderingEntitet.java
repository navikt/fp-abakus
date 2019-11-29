package no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding;

import java.math.BigDecimal;
import java.time.LocalDate;
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

import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "Gradering")
@Table(name = "IAY_GRADERING")
public class GraderingEntitet extends BaseEntitet implements Gradering, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GRADERING")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inntektsmelding_id", nullable = false, updatable = false)
    private Inntektsmelding inntektsmelding;

    @Embedded
    @ChangeTracked
    private DatoIntervallEntitet periode;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "arbeidstid_prosent", updatable = false, nullable = false)))
    @ChangeTracked
    private Stillingsprosent arbeidstidProsent;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    GraderingEntitet() {
    }

    private GraderingEntitet(DatoIntervallEntitet periode, Stillingsprosent arbeidstidProsent) {
        this.arbeidstidProsent = arbeidstidProsent;
        this.periode = periode;
    }

    public GraderingEntitet(LocalDate fom, LocalDate tom, BigDecimal arbeidstidProsent) {
        this(tom == null ? DatoIntervallEntitet.fraOgMed(fom) : DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom), new Stillingsprosent(arbeidstidProsent));
    }

    GraderingEntitet(Gradering gradering) {
        this(gradering.getPeriode(), gradering.getArbeidstidProsent());
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(periode);
    }

    void setInntektsmelding(Inntektsmelding inntektsmelding) {
        this.inntektsmelding = inntektsmelding;
    }

    @Override
    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    @Override
    public Stillingsprosent getArbeidstidProsent() {
        return arbeidstidProsent;
    }

    @Override
    public String toString() {
        return "GraderingEntitet{" +
            "id=" + id +
            ", periode=" + periode +
            ", arbeidstidProsent=" + arbeidstidProsent +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof GraderingEntitet)) return false;
        var that = (GraderingEntitet) o;
        return Objects.equals(periode, that.periode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode);
    }
}
