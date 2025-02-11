package no.nav.foreldrepenger.abakus.vedtak.domene;

import jakarta.persistence.*;
import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.abakus.iaygrunnlag.kodeverk.Inntektskategori;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;

import java.util.Objects;
import java.util.Optional;

@Entity(name = "VedtakYtelseAndel")
@Table(name = "VE_YTELSE_ANDEL")
public class VedtakYtelseAndel extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VE_YTELSE_ANDEL")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ytelse_anvist_id", nullable = false, updatable = false, unique = true)
    private YtelseAnvist ytelseAnvist;

    @Embedded
    @ChangeTracked
    private Arbeidsgiver arbeidsgiver;

    @Column(name = "arbeidsforhold_id")
    private String arbeidsforholdId;

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

    public VedtakYtelseAndel() {
        // hibernate
    }

    public VedtakYtelseAndel(VedtakYtelseAndel vedtakYtelseAndel) {
        vedtakYtelseAndel.getArbeidsgiver().ifPresent(this::setArbeidsgiver);
        this.dagsats = vedtakYtelseAndel.getDagsats();
        this.inntektskategori = vedtakYtelseAndel.getInntektskategori();
    }

    public Optional<Arbeidsgiver> getArbeidsgiver() {
        return Optional.ofNullable(arbeidsgiver);
    }

    public void setArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public void setArbeidsforholdId(String arbeidsforholdId) {
        this.arbeidsforholdId = arbeidsforholdId;
    }

    public Beløp getDagsats() {
        return dagsats;
    }

    public void setDagsats(Beløp dagsats) {
        this.dagsats = dagsats;
    }

    public Stillingsprosent getUtbetalingsgradProsent() {
        return utbetalingsgradProsent;
    }

    public void setUtbetalingsgradProsent(Stillingsprosent utbetalingsgradProsent) {
        this.utbetalingsgradProsent = utbetalingsgradProsent;
    }

    public Stillingsprosent getRefusjonsgradProsent() {
        return refusjonsgradProsent;
    }

    public void setRefusjonsgradProsent(Stillingsprosent refusjonsgradProsent) {
        this.refusjonsgradProsent = refusjonsgradProsent;
    }

    void setYtelseAnvist(YtelseAnvist ytelseAnvist) {
        this.ytelseAnvist = ytelseAnvist;
    }

    public Inntektskategori getInntektskategori() {
        return inntektskategori;
    }

    public void setInntektskategori(Inntektskategori inntektskategori) {
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
        VedtakYtelseAndel that = (VedtakYtelseAndel) o;
        return ytelseAnvist.equals(that.ytelseAnvist) && Objects.equals(arbeidsgiver, that.arbeidsgiver) && dagsats.equals(that.dagsats)
            && inntektskategori == that.inntektskategori;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ytelseAnvist, arbeidsgiver, dagsats, inntektskategori);
    }

    @Override
    public String toString() {
        return "VedtakYtelseFordeling{" + "id=" + id + ", ytelseAnvist=" + ytelseAnvist + ", arbeidsgiver=" + arbeidsgiver + ", dagsats=" + dagsats
            + ", inntektskategori=" + inntektskategori + ", versjon=" + versjon + '}';
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {arbeidsgiver, dagsats, inntektskategori};
        return IndexKeyComposer.createKey(keyParts);
    }
}
