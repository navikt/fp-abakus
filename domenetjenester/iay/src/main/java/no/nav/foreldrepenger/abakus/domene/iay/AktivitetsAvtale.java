package no.nav.foreldrepenger.abakus.domene.iay;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

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
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.vedtak.konfig.Tid;

@Table(name = "IAY_AKTIVITETS_AVTALE")
@Entity(name = "AktivitetsAvtale")
public class AktivitetsAvtale extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AKTIVITETS_AVTALE")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "yrkesaktivitet_id", nullable = false, updatable = false, unique = true)
    private Yrkesaktivitet yrkesaktivitet;

    @ChangeTracked
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "prosentsats")))
    private Stillingsprosent prosentsats;

    @Column(name = "beskrivelse")
    private String beskrivelse;

    @Embedded
    @ChangeTracked
    private IntervallEntitet periode;

    @ChangeTracked
    @Column(name = "siste_loennsendringsdato")
    private LocalDate sisteLønnsendringsdato;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    /**
     * Setter en periode brukt til overstyring av angitt periode (avledet fra saksbehandlers vurderinger). Benyttes kun transient (ved
     * filtrering av modellen)
     */
    @Transient
    private IntervallEntitet overstyrtPeriode;

    AktivitetsAvtale() {
        // hibernate
    }

    /**
     * Deep copy ctor
     */
    AktivitetsAvtale(AktivitetsAvtale aktivitetsAvtale) {
        this.prosentsats = aktivitetsAvtale.getProsentsats();
        this.beskrivelse = aktivitetsAvtale.getBeskrivelse();
        this.sisteLønnsendringsdato = aktivitetsAvtale.getSisteLønnsendringsdato();
        this.periode = aktivitetsAvtale.getPeriodeUtenOverstyring();
    }

    public AktivitetsAvtale(AktivitetsAvtale avtale, IntervallEntitet overstyrtPeriode) {
        this(avtale);
        this.overstyrtPeriode = overstyrtPeriode;
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {periode, sisteLønnsendringsdato};
        return IndexKeyComposer.createKey(keyParts);
    }

    /**
     * Avtalt prosentsats i avtalen
     *
     * @return prosent
     */
    public Stillingsprosent getProsentsats() {
        return prosentsats;
    }

    void setProsentsats(Stillingsprosent prosentsats) {
        this.prosentsats = prosentsats;
    }

    /**
     * Returner {@link #getProsentsats()} (skalert) eller null.
     */
    public BigDecimal getProsentsatsVerdi() {
        return prosentsats == null ? null : prosentsats.getVerdi();
    }

    /**
     * Periode
     *
     * @return hele perioden
     */
    public IntervallEntitet getPeriode() {
        return erOverstyrtPeriode() ? overstyrtPeriode : periode;
    }

    void setPeriode(IntervallEntitet periode) {
        this.periode = periode;
    }

    /**
     * Perioden til aktivitetsavtalen.
     * Tar Ikke hensyn til overstyring gjort i 5080.
     * <p>
     * Henter kun den originale perioden, ikke den overstyrte perioden.
     * Bruk heller {@link #getPeriode} i de fleste tilfeller
     *
     * @return Hele den originale perioden, uten overstyringer.
     */
    public IntervallEntitet getPeriodeUtenOverstyring() {
        return periode;
    }

    /**
     * Hvorvidt aktivitetsavtalen har en overstyrt periode eller ikke.
     *
     * @return boolean, true hvis overstyrt, false hvis ikke.
     * @deprecated FIXME - bør fjerne intern filtrering basert på initialisert transient overstyrt periode. Legg heller til egen Decorator
     * klasse som filtrerer output fra entitet
     */
    @Deprecated
    public boolean erOverstyrtPeriode() {
        return overstyrtPeriode != null;
    }

    /**
     * Siste lønnsendingsdato
     *
     * @return hele perioden
     */
    public LocalDate getSisteLønnsendringsdato() {
        return sisteLønnsendringsdato;
    }

    public boolean matcherPeriode(IntervallEntitet aktivitetsAvtale) {
        return getPeriode().equals(aktivitetsAvtale);
    }

    /**
     * Er avtallen løpende
     *
     * @return true/false
     */
    public boolean getErLøpende() {
        return Tid.TIDENES_ENDE.equals(getPeriode().getTomDato());
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    void setBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
    }

    void setYrkesaktivitet(Yrkesaktivitet yrkesaktivitet) {
        this.yrkesaktivitet = yrkesaktivitet;
    }

    void sisteLønnsendringsdato(LocalDate sisteLønnsendringsdato) {
        this.sisteLønnsendringsdato = sisteLønnsendringsdato;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof AktivitetsAvtale)) {
            return false;
        }
        AktivitetsAvtale that = (AktivitetsAvtale) o;
        return Objects.equals(beskrivelse, that.beskrivelse) && Objects.equals(prosentsats, that.prosentsats) && Objects.equals(periode, that.periode)
            && Objects.equals(overstyrtPeriode, that.overstyrtPeriode) && Objects.equals(sisteLønnsendringsdato, that.sisteLønnsendringsdato);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beskrivelse, prosentsats, periode, overstyrtPeriode, sisteLønnsendringsdato);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + "periode=" + periode + ", overstyrtPeriode=" + overstyrtPeriode + ", prosentsats=" + prosentsats
            + ", beskrivelse=" + beskrivelse + ", sisteLønnsendringsdato=" + sisteLønnsendringsdato + '>';
    }

    boolean hasValues() {
        return prosentsats != null || periode != null;
    }

    public boolean erAnsettelsesPeriode() {
        return (prosentsats == null || prosentsats.getVerdi() == null || prosentsats.erNulltall()) && sisteLønnsendringsdato == null;
    }
}
