package no.nav.foreldrepenger.abakus.vedtak.domene;

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

import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;

@Entity(name = "VedtakYtelseAnvistEntitet")
@Table(name = "VE_YTELSE_ANVIST")
public class YtelseAnvist extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VEDTAK_YTELSE_ANVIST")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ytelse_id", nullable = false, updatable = false, unique = true)
    private VedtakYtelse ytelse;

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
        this.anvistPeriode = IntervallEntitet.fraOgMedTilOgMed(ytelseAnvist.getAnvistFom(), ytelseAnvist.getAnvistTom());
        this.beløp = ytelseAnvist.getBeløp().orElse(null);
        this.dagsats = ytelseAnvist.getDagsats().orElse(null);
        this.utbetalingsgradProsent = ytelseAnvist.getUtbetalingsgradProsent().orElse(null);
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = { this.anvistPeriode };
        return IndexKeyComposer.createKey(keyParts);
    }

    public LocalDate getAnvistFom() {
        return anvistPeriode.getFomDato();
    }

    public LocalDate getAnvistTom() {
        return anvistPeriode.getTomDato();
    }

    public Optional<Stillingsprosent> getUtbetalingsgradProsent() {
        return Optional.ofNullable(utbetalingsgradProsent);
    }

    void setUtbetalingsgradProsent(Stillingsprosent utbetalingsgradProsent) {
        this.utbetalingsgradProsent = utbetalingsgradProsent;
    }

    public Optional<Beløp> getBeløp() {
        return Optional.ofNullable(beløp);
    }

    void setBeløp(Beløp beløp) {
        this.beløp = beløp;
    }

    public Optional<Beløp> getDagsats() {
        return Optional.ofNullable(dagsats);
    }

    void setDagsats(Beløp dagsats) {
        this.dagsats = dagsats;
    }

    void setAnvistPeriode(IntervallEntitet periode) {
        this.anvistPeriode = periode;
    }

    void setYtelse(VedtakYtelse ytelse) {
        this.ytelse = ytelse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof YtelseAnvist)) return false;
        var that = (YtelseAnvist) o;
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
