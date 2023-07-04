package no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
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
import no.nav.foreldrepenger.abakus.typer.Beløp;

@Entity(name = "Refusjon")
@Table(name = "IAY_REFUSJON")
public class Refusjon extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_REFUSJON")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inntektsmelding_id", nullable = false, updatable = false)
    private Inntektsmelding inntektsmelding;

    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "refusjonsbeloep_mnd", nullable = false)))
    @ChangeTracked
    private Beløp refusjonsbeløpMnd;

    @Column(name = "fom", nullable = false)
    @ChangeTracked
    private LocalDate fom;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public Refusjon() {
    }

    public Refusjon(BigDecimal refusjonsbeløpMnd, LocalDate fom) {
        this.refusjonsbeløpMnd = refusjonsbeløpMnd == null ? null : new Beløp(refusjonsbeløpMnd);
        this.fom = fom;
    }

    Refusjon(Refusjon refusjon) {
        this.refusjonsbeløpMnd = refusjon.getRefusjonsbeløp();
        this.fom = refusjon.getFom();
    }

    public void setInntektsmelding(Inntektsmelding inntektsmelding) {
        this.inntektsmelding = inntektsmelding;
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {fom, refusjonsbeløpMnd};
        return IndexKeyComposer.createKey(keyParts);
    }

    public Beløp getRefusjonsbeløp() {
        return refusjonsbeløpMnd;
    }

    public LocalDate getFom() {
        return fom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Refusjon)) {
            return false;
        }
        var that = (Refusjon) o;
        return Objects.equals(refusjonsbeløpMnd, that.refusjonsbeløpMnd) && Objects.equals(fom, that.fom);
    }

    @Override
    public int hashCode() {

        return Objects.hash(refusjonsbeløpMnd, fom);
    }
}
