package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Objects;
import java.util.Optional;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Convert;
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
import no.nav.abakus.iaygrunnlag.kodeverk.Inntektskategori;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.foreldrepenger.abakus.vedtak.domene.InntektskategoriKodeverdiConverter;


@Entity(name = "YtelseAnvistAndel")
@Table(name = "IAY_YTELSE_ANVIST_ANDEL")
public class YtelseAnvistAndel extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_YTELSE_ANVIST_ANDEL")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ytelse_anvist_id", nullable = false, updatable = false, unique = true)
    private YtelseAnvist ytelseAnvist;

    @Embedded
    @ChangeTracked
    private Arbeidsgiver arbeidsgiver;

    @Embedded
    private InternArbeidsforholdRef arbeidsforholdRef;

    /**
     * Netto dagsats som tilsvarer grunnlagsdagsats * utbetalingsgrad
     */
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "dagsats", nullable = false)))
    @ChangeTracked
    private Beløp dagsats;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "utbetalingsgrad_prosent")))
    @ChangeTracked
    private Stillingsprosent utbetalingsgradProsent;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "refusjonsgrad_prosent")))
    @ChangeTracked
    private Stillingsprosent refusjonsgradProsent;

    @Convert(converter = InntektskategoriKodeverdiConverter.class)
    @Column(name = "inntektskategori", nullable = false, updatable = false)
    private Inntektskategori inntektskategori = Inntektskategori.UDEFINERT;


    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public YtelseAnvistAndel() {
        // hibernate
    }

    public YtelseAnvistAndel(YtelseAnvistAndel ytelseAnvistAndel) {
        ytelseAnvistAndel.getArbeidsgiver().ifPresent(this::setArbeidsgiver);
        this.dagsats = ytelseAnvistAndel.getDagsats();
        this.inntektskategori = ytelseAnvistAndel.getInntektskategori();
        this.refusjonsgradProsent = ytelseAnvistAndel.getRefusjonsgradProsent();
        this.utbetalingsgradProsent = ytelseAnvistAndel.getUtbetalingsgradProsent();
        this.arbeidsforholdRef = ytelseAnvistAndel.getArbeidsforholdRef();
    }

    public Optional<Arbeidsgiver> getArbeidsgiver() {
        return Optional.ofNullable(arbeidsgiver);
    }

    void setArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }


    public InternArbeidsforholdRef getArbeidsforholdRef() {
        return arbeidsforholdRef != null ? arbeidsforholdRef : InternArbeidsforholdRef.nullRef();
    }

    void setArbeidsforholdRef(InternArbeidsforholdRef arbeidsforholdRef) {
        this.arbeidsforholdRef = arbeidsforholdRef;
    }

    public Beløp getDagsats() {
        return dagsats;
    }

    void setDagsats(Beløp dagsats) {
        this.dagsats = dagsats;
    }

    public Stillingsprosent getUtbetalingsgradProsent() {
        return utbetalingsgradProsent;
    }

    void setUtbetalingsgradProsent(Stillingsprosent utbetalingsgradProsent) {
        this.utbetalingsgradProsent = utbetalingsgradProsent;
    }

    public Stillingsprosent getRefusjonsgradProsent() {
        return refusjonsgradProsent;
    }

    void setRefusjonsgradProsent(Stillingsprosent refusjonsgradProsent) {
        this.refusjonsgradProsent = refusjonsgradProsent;
    }

    void setYtelseAnvist(YtelseAnvist ytelseAnvist) {
        this.ytelseAnvist = ytelseAnvist;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    void setInntektskategori(Inntektskategori inntektskategori) {
        this.inntektskategori = inntektskategori;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        YtelseAnvistAndel that = (YtelseAnvistAndel) o;
        return ytelseAnvist.equals(that.ytelseAnvist) && Objects.equals(arbeidsgiver, that.arbeidsgiver) && dagsats.equals(that.dagsats)
            && inntektskategori == that.inntektskategori && utbetalingsgradProsent.equals(that.utbetalingsgradProsent) && refusjonsgradProsent.equals(
            that.refusjonsgradProsent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ytelseAnvist, arbeidsgiver, dagsats, inntektskategori, utbetalingsgradProsent, refusjonsgradProsent);
    }

    @Override
    public String toString() {
        return "YtelseAnvistAndel{" + "id=" + id + ", ytelseAnvist=" + ytelseAnvist + ", arbeidsgiver=" + arbeidsgiver + ", arbeidsforholdRef="
            + arbeidsforholdRef + ", dagsats=" + dagsats + ", utbetalingsgradProsent=" + utbetalingsgradProsent + ", refusjonsgradProsent="
            + refusjonsgradProsent + ", inntektskategori=" + inntektskategori + ", versjon=" + versjon + '}';
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {arbeidsgiver, dagsats, inntektskategori, utbetalingsgradProsent, refusjonsgradProsent};
        return IndexKeyComposer.createKey(keyParts);
    }
}
