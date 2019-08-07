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

import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.AntallTimer;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.konfig.Tid;

@Table(name = "IAY_AKTIVITETS_AVTALE")
@Entity(name = "AktivitetsAvtale")
public class AktivitetsAvtaleEntitet extends BaseEntitet implements AktivitetsAvtale, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AKTIVITETS_AVTALE")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "yrkesaktivitet_id", nullable = false, updatable = false, unique = true)
    private YrkesaktivitetEntitet yrkesaktivitet;

    @ChangeTracked
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "prosentsats")))
    private Stillingsprosent prosentsats;

    /** TODO (FC): Se om vi kan bli kvitt antallTimer. Brukes bare til å sjekke om det finnes verdi i {@link #erAnsettelsesPeriode()}. */
    @ChangeTracked
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "antall_timer")))
    private AntallTimer antallTimer;

    /** TODO (FC): Se om vi kan bli kvitt antallTimerFulltid. Brukes bare til å sjekke om det finnes verdi i {@link #erAnsettelsesPeriode()}. */
    @ChangeTracked
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "antall_timer_fulltid")))
    private AntallTimer antallTimerFulltid;

    @Column(name = "beskrivelse")
    private String beskrivelse;

    @Embedded
    @ChangeTracked
    private DatoIntervallEntitet periode;

    @ChangeTracked
    @Column(name = "siste_loennsendringsdato")
    private LocalDate sisteLønnsendringsdato;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    /**
     * @deprecated FIXME - bør fjerne intern filtrering basert på initialisert transient skjæringstidspunkt.  Legg heller til egen Decorator klasse som filtrerer output fra entitet
     */
    @Deprecated
    @Transient
    private LocalDate skjæringstidspunkt;

    /**
     * @deprecated FIXME - bør fjerne intern filtrering basert på initialisert transient skjæringstidspunkt.  Legg heller til egen Decorator klasse som filtrerer output fra entitet
     */
    @Deprecated
    @Transient
    private boolean ventreSideAvSkjæringstidspunkt;

    /**
     * @deprecated FIXME - bør fjerne intern filtrering basert på initialisert transient skjæringstidspunkt.  Legg heller til egen Decorator klasse som filtrerer output fra entitet
     */
    @Deprecated
    @Transient
    private DatoIntervallEntitet overstyrtPeriode;

    AktivitetsAvtaleEntitet() {
        // hibernate
    }

    /**
     * Deep copy ctor
     */
    AktivitetsAvtaleEntitet(AktivitetsAvtale aktivitetsAvtale) {
        AktivitetsAvtaleEntitet entitet = (AktivitetsAvtaleEntitet) aktivitetsAvtale; // NOSONAR
        this.prosentsats = entitet.prosentsats;
        this.beskrivelse = entitet.beskrivelse;
        this.periode = entitet.periode;
        this.sisteLønnsendringsdato = entitet.sisteLønnsendringsdato;
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(periode, sisteLønnsendringsdato);
    }

    @Override
    public Stillingsprosent getProsentsats() {
        return prosentsats;
    }

    @Override
    public BigDecimal getProsentsatsVerdi() {
        return prosentsats == null ? null : prosentsats.getVerdi();
    }

    @Override
    public AntallTimer getAntallTimer() {
        return antallTimer;
    }

    void setAntallTimer(AntallTimer antallTimer) {
        this.antallTimer = antallTimer;
    }

    @Override
    public AntallTimer getAntallTimerFulltid() {
        return antallTimerFulltid;
    }

    void setAntallTimerFulltid(AntallTimer antallTimerFulltid) {
        this.antallTimerFulltid = antallTimerFulltid;
    }

    void setProsentsats(Stillingsprosent prosentsats) {
        this.prosentsats = prosentsats;
    }

    @Override
    public DatoIntervallEntitet getPeriode() {
        return erOverstyrtPeriode() ? overstyrtPeriode : periode;
    }

    /**
     * Henter kun den originale perioden, ikke den overstyrte perioden.
     * Bruk heller {@link #getPeriode} i de fleste tilfeller
     * @return Hele den originale perioden, uten overstyringer.
     */
    public DatoIntervallEntitet getPeriodeUtenOverstyring() {
        return periode;
    }

    void setPeriode(DatoIntervallEntitet periode) {
        this.periode = periode;
    }

    /**
     * @deprecated FIXME - bør fjerne intern filtrering basert på initialisert transient overstyrt periode.  Legg heller til egen Decorator klasse som filtrerer output fra entitet
     */
    @Deprecated
    @Override
    public boolean erOverstyrtPeriode() {
        return overstyrtPeriode != null;
    }

    @Override
    public LocalDate getSisteLønnsendringsdato() {
        return sisteLønnsendringsdato;
    }

    @Override
    public boolean matcherPeriode(DatoIntervallEntitet aktivitetsAvtale) {
        return getPeriode().equals(aktivitetsAvtale);
    }

    @Override
    public boolean getErLøpende() {
        return Tid.TIDENES_ENDE.equals(getPeriode().getTomDato());
    }

    @Override
    public String getBeskrivelse() {
        return beskrivelse;
    }

    void setBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
    }

    @Override
    public YrkesaktivitetEntitet getYrkesaktivitet() {
        return yrkesaktivitet;
    }

    void setYrkesaktivitet(YrkesaktivitetEntitet yrkesaktivitet) {
        this.yrkesaktivitet = yrkesaktivitet;
    }

    /**
     * @deprecated FIXME - bør fjerne intern filtrering basert på initialisert transient overstyrt periode.  Legg heller til egen Decorator klasse som filtrerer output fra entitet
     */
    @Deprecated
    void setOverstyrtPeriode(DatoIntervallEntitet overstyrtPeriode) {
        this.overstyrtPeriode = overstyrtPeriode;
    }

    void sisteLønnsendringsdato(LocalDate sisteLønnsendringsdato) {
        this.sisteLønnsendringsdato = sisteLønnsendringsdato;
    }

    /**
     * @deprecated FIXME - bør fjerne intern filtrering basert på initialisert transient skjæringstidspunkt.  Legg heller til egen Decorator klasse som filtrerer output fra entitet
     */
    @Deprecated
    boolean skalMedEtterSkjæringstidspunktVurdering() {
        if (skjæringstidspunkt != null) {
            if (ventreSideAvSkjæringstidspunkt) {
                return getPeriode().getFomDato().isBefore(skjæringstidspunkt);
            } else {
                return getPeriode().getFomDato().isAfter(skjæringstidspunkt.minusDays(1)) ||
                    getPeriode().getFomDato().isBefore(skjæringstidspunkt) && getPeriode().getTomDato().isAfter(skjæringstidspunkt.minusDays(1));
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof AktivitetsAvtaleEntitet)) return false;
        AktivitetsAvtaleEntitet that = (AktivitetsAvtaleEntitet) o;
        return Objects.equals(antallTimer, that.antallTimer) &&
            Objects.equals(antallTimerFulltid, that.antallTimerFulltid) &&
            Objects.equals(beskrivelse, that.beskrivelse) &&
            Objects.equals(prosentsats, that.prosentsats) &&
            Objects.equals(periode, that.periode) &&
            Objects.equals(overstyrtPeriode, that.overstyrtPeriode) &&
            Objects.equals(sisteLønnsendringsdato, that.sisteLønnsendringsdato);
    }

    @Override
    public int hashCode() {
        return Objects.hash(antallTimer, antallTimerFulltid, beskrivelse, prosentsats, periode, overstyrtPeriode, sisteLønnsendringsdato);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
            "antallTimer=" + antallTimer + //$NON-NLS-1$
            ", antallTimerFulltid=" + antallTimerFulltid + //$NON-NLS-1$
            ", periode=" + periode + //$NON-NLS-1$
            ", overstyrtPeriode=" + overstyrtPeriode + //$NON-NLS-1$
            ", prosentsats=" + prosentsats + //$NON-NLS-1$
            ", beskrivelse=" + beskrivelse + //$NON-NLS-1$
            ", sisteLønnsendringsdato="+sisteLønnsendringsdato + //$NON-NLS-1$
            '>';
    }

    boolean hasValues() {
        return prosentsats != null || periode != null;
    }

    @Override
    public boolean erAnsettelsesPeriode() {
        return (antallTimer == null || antallTimer.getVerdi() == null)
            && (antallTimerFulltid == null || antallTimerFulltid.getVerdi() == null)
            && (prosentsats == null || prosentsats.erNulltall())
            && sisteLønnsendringsdato == null;
    }

    /**
     * @deprecated FIXME - bør fjerne intern filtrering basert på initialisert transient skjæringstidspunkt.  Legg heller til egen Decorator klasse som filtrerer output fra entitet
     */
    @Deprecated
    void setSkjæringstidspunkt(LocalDate skjæringstidspunkt, boolean ventreSide) {
        this.skjæringstidspunkt = skjæringstidspunkt;
        this.ventreSideAvSkjæringstidspunkt = ventreSide;
    }

}
