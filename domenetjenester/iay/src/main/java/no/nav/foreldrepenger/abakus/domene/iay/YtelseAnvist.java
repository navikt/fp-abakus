package no.nav.foreldrepenger.abakus.domene.iay;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;

@Entity(name = "YtelseAnvistEntitet")
@Table(name = "IAY_YTELSE_ANVIST")
public class YtelseAnvist extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_YTELSE_ANVIST")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ytelse_id", nullable = false, updatable = false, unique = true)
    private Ytelse ytelse;

    @OneToMany(mappedBy = "ytelseAnvist")
    @ChangeTracked
    private Set<YtelseAnvistAndel> ytelseAnvistAndeler = new LinkedHashSet<>();

    @Embedded
    @ChangeTracked
    private IntervallEntitet anvistPeriode;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "beloep")))
    @ChangeTracked
    private Beløp beløp;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "dagsats")))
    @ChangeTracked
    private Beløp dagsats;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "utbetalingsgrad_prosent")))
    @ChangeTracked
    private Stillingsprosent utbetalingsgradProsent;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public YtelseAnvist() {
        // hibernate
    }

    public YtelseAnvist(YtelseAnvist ytelseAnvist) {
        this.anvistPeriode =
                IntervallEntitet.fraOgMedTilOgMed(ytelseAnvist.getAnvistFOM(), ytelseAnvist.getAnvistTOM());
        this.beløp = ytelseAnvist.getBeløp().orElse(null);
        this.dagsats = ytelseAnvist.getDagsats().orElse(null);
        this.utbetalingsgradProsent = ytelseAnvist.getUtbetalingsgradProsent().orElse(null);
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {this.anvistPeriode};
        return IndexKeyComposer.createKey(keyParts);
    }

    public LocalDate getAnvistFOM() {
        return anvistPeriode.getFomDato();
    }

    public LocalDate getAnvistTOM() {
        return anvistPeriode.getTomDato();
    }

    public Optional<Stillingsprosent> getUtbetalingsgradProsent() {
        return Optional.ofNullable(utbetalingsgradProsent);
    }

    public void setUtbetalingsgradProsent(Stillingsprosent utbetalingsgradProsent) {
        this.utbetalingsgradProsent = utbetalingsgradProsent;
    }

    public Optional<Beløp> getBeløp() {
        return Optional.ofNullable(beløp);
    }

    public void setBeløp(Beløp beløp) {
        this.beløp = beløp;
    }

    public Optional<Beløp> getDagsats() {
        return Optional.ofNullable(dagsats);
    }

    public void setDagsats(Beløp dagsats) {
        this.dagsats = dagsats;
    }

    void setAnvistPeriode(IntervallEntitet periode) {
        this.anvistPeriode = periode;
    }

    public void setYtelse(Ytelse ytelse) {
        this.ytelse = ytelse;
    }

    public Set<YtelseAnvistAndel> getYtelseAnvistAndeler() {
        return ytelseAnvistAndeler;
    }

    void leggTilYtelseAnvistAndel(YtelseAnvistAndel ytelseAnvistAndel) {
        ytelseAnvistAndel.setYtelseAnvist(this);
        this.ytelseAnvistAndeler.add(ytelseAnvistAndel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof YtelseAnvist)) {
            return false;
        }
        YtelseAnvist that = (YtelseAnvist) o;
        return Objects.equals(anvistPeriode, that.anvistPeriode)
                && Objects.equals(beløp, that.beløp)
                && Objects.equals(dagsats, that.dagsats)
                && Objects.equals(utbetalingsgradProsent, that.utbetalingsgradProsent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anvistPeriode, beløp, dagsats, utbetalingsgradProsent);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + "periode=" + anvistPeriode + ", beløp=" + beløp + ", dagsats="
                + dagsats + ", utbetalingsgradProsent=" + utbetalingsgradProsent + '>';
    }
}
