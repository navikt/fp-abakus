package no.nav.foreldrepenger.abakus.lonnskomp.domene;

import jakarta.persistence.*;
import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.Beløp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Entity(name = "LonnskompAnvistEntitet")
@Table(name = "LONNSKOMP_ANVIST")
public class LønnskompensasjonAnvist extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_LONNSKOMP_ANVIST")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vedtak_id", nullable = false, updatable = false, unique = true)
    private LønnskompensasjonVedtak vedtak;

    @Embedded
    @ChangeTracked
    private IntervallEntitet anvistPeriode;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "beloep")))
    @ChangeTracked
    private Beløp beløp;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public LønnskompensasjonAnvist() {
        // hibernate
    }

    public LønnskompensasjonAnvist(LønnskompensasjonAnvist ytelseAnvist) {
        this.anvistPeriode = IntervallEntitet.fraOgMedTilOgMed(ytelseAnvist.getAnvistFom(), ytelseAnvist.getAnvistTom());
        this.beløp = ytelseAnvist.getBeløp().orElse(null);
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {this.anvistPeriode};
        return IndexKeyComposer.createKey(keyParts);
    }

    public LocalDate getAnvistFom() {
        return anvistPeriode.getFomDato();
    }

    public LocalDate getAnvistTom() {
        return anvistPeriode.getTomDato();
    }

    public Optional<Beløp> getBeløp() {
        return Optional.ofNullable(beløp);
    }

    void setBeløp(Beløp beløp) {
        this.beløp = beløp;
    }

    void setAnvistPeriode(IntervallEntitet periode) {
        this.anvistPeriode = periode;
    }

    void setVedtak(LønnskompensasjonVedtak vedtak) {
        this.vedtak = vedtak;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof LønnskompensasjonAnvist)) {
            return false;
        }
        var that = (LønnskompensasjonAnvist) o;
        return Objects.equals(anvistPeriode, that.anvistPeriode) && Objects.equals(beløp, that.beløp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anvistPeriode, beløp);
    }

    @Override
    public String toString() {
        return "LønnskompensasjonAnvistEntitet{" + "periode=" + anvistPeriode + ", beløp=" + beløp + '}';
    }

    public static class LønnskompensasjonAnvistBuilder {
        private final LønnskompensasjonAnvist anvist;

        LønnskompensasjonAnvistBuilder(LønnskompensasjonAnvist anvist) {
            this.anvist = anvist;
        }

        public static LønnskompensasjonAnvistBuilder ny() {
            return new LønnskompensasjonAnvistBuilder(new LønnskompensasjonAnvist());
        }

        public LønnskompensasjonAnvistBuilder medBeløp(BigDecimal beløp) {
            if (beløp != null) {
                this.anvist.setBeløp(new Beløp(beløp));
            }
            return this;
        }

        public LønnskompensasjonAnvistBuilder medAnvistPeriode(IntervallEntitet intervallEntitet) {
            this.anvist.setAnvistPeriode(intervallEntitet);
            return this;
        }

        public LønnskompensasjonAnvist build() {
            return anvist;
        }

    }
}
