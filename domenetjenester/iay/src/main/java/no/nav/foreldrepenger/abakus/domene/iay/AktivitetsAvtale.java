package no.nav.foreldrepenger.abakus.domene.iay;

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
import javax.persistence.Transient;
import javax.persistence.Version;

import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.DiffIgnore;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.AntallTimer;
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

    /**
     * TODO (FC): Se om vi kan bli kvitt antallTimer. Brukes bare til å sjekke om det finnes verdi i {@link #erAnsettelsesPeriode()}.
     */
    @DiffIgnore
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "antall_timer")))
    private AntallTimer antallTimer;

    /**
     * TODO (FC): Se om vi kan bli kvitt antallTimerFulltid. Brukes bare til å sjekke om det finnes verdi i {@link #erAnsettelsesPeriode()}.
     */
    @DiffIgnore
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "antall_timer_fulltid")))
    private AntallTimer antallTimerFulltid;

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
     * For timelønnede så vil antallet timer i arbeidsavtalen være satt her
     *
     * @return antall timer
     */
    public AntallTimer getAntallTimer() {
        return antallTimer;
    }

    void setAntallTimer(AntallTimer antallTimer) {
        this.antallTimer = antallTimer;
    }

    /**
     * Antall timer som tilsvarer fulltid (f.eks 40 timer)
     *
     * @return antall timer
     */
    public AntallTimer getAntallTimerFulltid() {
        return antallTimerFulltid;
    }

    void setAntallTimerFulltid(AntallTimer antallTimerFulltid) {
        this.antallTimerFulltid = antallTimerFulltid;
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
        return Objects.equals(antallTimer, that.antallTimer) && Objects.equals(antallTimerFulltid, that.antallTimerFulltid) && Objects.equals(
            beskrivelse, that.beskrivelse) && Objects.equals(prosentsats, that.prosentsats) && Objects.equals(periode, that.periode)
            && Objects.equals(overstyrtPeriode, that.overstyrtPeriode) && Objects.equals(sisteLønnsendringsdato, that.sisteLønnsendringsdato);
    }

    @Override
    public int hashCode() {
        return Objects.hash(antallTimer, antallTimerFulltid, beskrivelse, prosentsats, periode, overstyrtPeriode, sisteLønnsendringsdato);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" +
            "antallTimer=" + antallTimer +
            ", antallTimerFulltid=" + antallTimerFulltid +
            ", periode=" + periode +
            ", overstyrtPeriode=" + overstyrtPeriode +
            ", prosentsats=" + prosentsats +
            ", beskrivelse=" + beskrivelse +
            ", sisteLønnsendringsdato=" + sisteLønnsendringsdato +
            '>';
    }

    boolean hasValues() {
        return prosentsats != null || periode != null;
    }

    public boolean erAnsettelsesPeriode() {
        return (antallTimer == null || antallTimer.getVerdi() == null) && (antallTimerFulltid == null || antallTimerFulltid.getVerdi() == null) && (
            prosentsats == null || prosentsats.getVerdi() == null || prosentsats.erNulltall()) && sisteLønnsendringsdato == null;
    }
}
