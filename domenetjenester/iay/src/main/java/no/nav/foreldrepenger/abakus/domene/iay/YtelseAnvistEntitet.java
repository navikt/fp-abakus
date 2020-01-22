package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

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
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "YtelseAnvistEntitet")
@Table(name = "IAY_YTELSE_ANVIST")
public class YtelseAnvistEntitet extends BaseEntitet implements YtelseAnvist, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_YTELSE_ANVIST")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ytelse_id", nullable = false, updatable = false, unique = true)
    private YtelseEntitet ytelse;

    @Embedded
    @ChangeTracked
    private DatoIntervallEntitet anvistPeriode;

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

    public YtelseAnvistEntitet() {
        // hibernate
    }

    public YtelseAnvistEntitet(YtelseAnvist ytelseAnvist) {
        this.anvistPeriode = DatoIntervallEntitet.fraOgMedTilOgMed(ytelseAnvist.getAnvistFOM(), ytelseAnvist.getAnvistTOM());
        this.beløp = ytelseAnvist.getBeløp().orElse(null);
        this.dagsats = ytelseAnvist.getDagsats().orElse(null);
        this.utbetalingsgradProsent = ytelseAnvist.getUtbetalingsgradProsent().orElse(null);
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(this.anvistPeriode);
    }

    @Override
    public LocalDate getAnvistFOM() {
        return anvistPeriode.getFomDato();
    }

    @Override
    public LocalDate getAnvistTOM() {
        return anvistPeriode.getTomDato();
    }

    @Override
    public Optional<Stillingsprosent> getUtbetalingsgradProsent() {
        return Optional.ofNullable(utbetalingsgradProsent);
    }

    @Override
    public Optional<Beløp> getBeløp() {
        return Optional.ofNullable(beløp);
    }

    @Override
    public Optional<Beløp> getDagsats() {
        return Optional.ofNullable(dagsats);
    }

    public void setBeløp(Beløp beløp) {
        this.beløp = beløp;
    }

    public void setDagsats(Beløp dagsats) {
        this.dagsats = dagsats;
    }

    void setAnvistPeriode(DatoIntervallEntitet periode) {
        this.anvistPeriode = periode;
    }

    public void setUtbetalingsgradProsent(Stillingsprosent utbetalingsgradProsent) {
        this.utbetalingsgradProsent = utbetalingsgradProsent;
    }

    public void setYtelse(YtelseEntitet ytelse) {
        this.ytelse = ytelse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof YtelseAnvistEntitet)) return false;
        YtelseAnvistEntitet that = (YtelseAnvistEntitet) o;
        return Objects.equals(anvistPeriode, that.anvistPeriode) &&
            Objects.equals(beløp, that.beløp) &&
            Objects.equals(dagsats, that.dagsats) &&
            Objects.equals(utbetalingsgradProsent, that.utbetalingsgradProsent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anvistPeriode, beløp, dagsats, utbetalingsgradProsent);
    }

    @Override
    public String toString() {
        return "YtelseAnvistEntitet{" +
            "periode=" + anvistPeriode +
            ", beløp=" + beløp +
            ", dagsats=" + dagsats +
            ", utbetalingsgradProsent=" + utbetalingsgradProsent +
            '}';
    }
}
