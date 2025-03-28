package no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding;

import java.time.Duration;
import java.time.LocalDate;
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
import jakarta.persistence.Version;
import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;

@Entity(name = "Fravær")
@Table(name = "IAY_FRAVAER")
public class Fravær extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_IAY_FRAVAER")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inntektsmelding_id", nullable = false, updatable = false)
    private Inntektsmelding inntektsmelding;

    @Embedded
    @ChangeTracked
    private IntervallEntitet periode;

    /**
     * tid oppgittFravær per dag. Hvis ikke oppgitt antas hele dagen å telle med.
     */
    @ChangeTracked
    @Column(name = "varighet_per_dag", nullable = true)
    private Duration varighetPerDag;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    Fravær() {
        // Plattform (CDI, Hibernate, Jackson)
    }

    public Fravær(LocalDate fom, LocalDate tom, Duration varighetPerDag) {
        this.varighetPerDag = varighetPerDag;
        this.periode = IntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }

    Fravær(Fravær entity) {
        this.periode = entity.getPeriode();
        this.varighetPerDag = entity.getVarighetPerDag();
    }

    public Duration getVarighetPerDag() {
        return varighetPerDag;
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {periode};
        return IndexKeyComposer.createKey(keyParts);
    }

    void setInntektsmelding(Inntektsmelding inntektsmelding) {
        this.inntektsmelding = inntektsmelding;
    }

    public IntervallEntitet getPeriode() {
        return periode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Fravær)) {
            return false;
        }
        var that = (Fravær) o;
        return Objects.equals(periode, that.periode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode);
    }

    @Override
    public String toString() {
        return "NaturalYtelseEntitet{" + "id=" + id + ", periode=" + periode + ", varighetPerDag=" + varighetPerDag + '}';
    }
}
