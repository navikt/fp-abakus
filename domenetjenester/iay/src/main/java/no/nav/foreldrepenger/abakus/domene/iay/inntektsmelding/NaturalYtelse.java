package no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding;

import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.abakus.iaygrunnlag.kodeverk.NaturalytelseType;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.iay.jpa.NaturalytelseTypeKodeverdiConverter;
import no.nav.foreldrepenger.abakus.typer.Beløp;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity(name = "NaturalYtelse")
@Table(name = "IAY_NATURAL_YTELSE")
public class NaturalYtelse extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_NATURAL_YTELSE")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inntektsmelding_id", nullable = false, updatable = false)
    private Inntektsmelding inntektsmelding;

    @Embedded
    @ChangeTracked
    private IntervallEntitet periode;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "beloep_mnd", nullable = false)))
    @ChangeTracked
    private Beløp beloepPerMnd;

    @Convert(converter = NaturalytelseTypeKodeverdiConverter.class)
    @Column(name = "natural_ytelse_type", nullable = false, updatable = false)
    private NaturalytelseType type = NaturalytelseType.UDEFINERT;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    NaturalYtelse() {
    }

    public NaturalYtelse(LocalDate fom, LocalDate tom, BigDecimal beloepPerMnd, NaturalytelseType type) {
        this.beloepPerMnd = new Beløp(beloepPerMnd);
        this.type = type;
        this.periode = IntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }

    NaturalYtelse(NaturalYtelse naturalYtelse) {
        this.periode = naturalYtelse.getPeriode();
        this.beloepPerMnd = naturalYtelse.getBeloepPerMnd();
        this.type = naturalYtelse.getType();
    }

    public NaturalYtelse(IntervallEntitet datoIntervall, BigDecimal beløpPerMnd, NaturalytelseType naturalytelseType) {
        this(datoIntervall.getFomDato(), datoIntervall.getTomDato(), beløpPerMnd, naturalytelseType);
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {type, periode};
        return IndexKeyComposer.createKey(keyParts);
    }

    void setInntektsmelding(Inntektsmelding inntektsmelding) {
        this.inntektsmelding = inntektsmelding;
    }

    public IntervallEntitet getPeriode() {
        return periode;
    }

    public Beløp getBeloepPerMnd() {
        return beloepPerMnd;
    }

    public NaturalytelseType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof NaturalYtelse)) {
            return false;
        }
        var that = (NaturalYtelse) o;
        return Objects.equals(periode, that.periode) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, type);
    }

    @Override
    public String toString() {
        return "NaturalYtelseEntitet{" + "id=" + id + ", periode=" + periode + ", beloepPerMnd=" + beloepPerMnd + ", type=" + type + '}';
    }
}
