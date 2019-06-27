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

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "NaturalYtelse")
@Table(name = "IAY_NATURAL_YTELSE")
public class NaturalYtelseEntitet extends BaseEntitet implements NaturalYtelse, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_NATURAL_YTELSE")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inntektsmelding_id", nullable = false, updatable = false)
    private InntektsmeldingEntitet inntektsmelding;

    @Embedded
    @ChangeTracked
    private DatoIntervallEntitet periode;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "beloep_mnd", nullable = false)))
    @ChangeTracked
    private Beløp beloepPerMnd;

    @ManyToOne
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + NaturalYtelseType.DISCRIMINATOR + "'")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "natural_ytelse_type", referencedColumnName = "kode")),
    })
    private NaturalYtelseType type = NaturalYtelseType.UDEFINERT;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    NaturalYtelseEntitet() {
    }

    public NaturalYtelseEntitet(LocalDate fom, LocalDate tom, BigDecimal beloepPerMnd, NaturalYtelseType type) {
        this.beloepPerMnd = new Beløp(beloepPerMnd);
        this.type = type;
        this.periode = DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }

    NaturalYtelseEntitet(NaturalYtelse naturalYtelse) {
        this.periode = naturalYtelse.getPeriode();
        this.beloepPerMnd = naturalYtelse.getBeloepPerMnd();
        this.type = naturalYtelse.getType();
    }

    public NaturalYtelseEntitet(DatoIntervallEntitet datoIntervall, BigDecimal beløpPerMnd, NaturalYtelseType naturalYtelseType) {
        this(datoIntervall.getFomDato(), datoIntervall.getTomDato(), beløpPerMnd, naturalYtelseType);
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(type, periode);
    }

    void setInntektsmelding(InntektsmeldingEntitet inntektsmelding) {
        this.inntektsmelding = inntektsmelding;
    }

    @Override
    public DatoIntervallEntitet getPeriode() {
        return periode;
    }

    @Override
    public Beløp getBeloepPerMnd() {
        return beloepPerMnd;
    }

    @Override
    public NaturalYtelseType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof NaturalYtelseEntitet)) return false;
        var that = (NaturalYtelseEntitet) o;
        return Objects.equals(periode, that.periode) &&
            Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, type);
    }

    @Override
    public String toString() {
        return "NaturalYtelseEntitet{" +
            "id=" + id +
            ", periode=" + periode +
            ", beloepPerMnd=" + beloepPerMnd +
            ", type=" + type +
            '}';
    }
}
