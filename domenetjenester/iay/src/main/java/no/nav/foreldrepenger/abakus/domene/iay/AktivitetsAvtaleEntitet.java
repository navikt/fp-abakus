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

    @Transient
    private LocalDate skjæringstidspunkt;
    @Transient
    private boolean ventreSideAvSkjæringstidspunkt;
    @Transient
    private DatoIntervallEntitet overstyrtPeriode;

    AktivitetsAvtaleEntitet() {
        // hibernate
    }

    /**
     * Deep copy ctor
     */
    AktivitetsAvtaleEntitet(AktivitetsAvtale aktivitetsAvtale) {
        AktivitetsAvtaleEntitet entitet = (AktivitetsAvtaleEntitet) aktivitetsAvtale; //NOSONAR
        this.prosentsats = entitet.prosentsats;
        this.beskrivelse = entitet.beskrivelse;
        this.periode = entitet.periode;
        this.sisteLønnsendringsdato = entitet.sisteLønnsendringsdato;
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(periode, prosentsats);
    }

    @Override
    public Stillingsprosent getProsentsats() {
        return prosentsats;
    }
    
    @Override
    public BigDecimal getProsentsatsVerdi() {
        return prosentsats==null?null:prosentsats.getVerdi();
    }

    void setProsentsats(Stillingsprosent prosentsats) {
        this.prosentsats = prosentsats;
    }

    @Override
    public LocalDate getFraOgMed() {
        return periode.getFomDato();
    }

    @Override
    public DatoIntervallEntitet getPeriode() {
        return overstyrtPeriode == null ? periode : overstyrtPeriode;
    }

    void setPeriode(DatoIntervallEntitet periode) {
        this.periode = periode;
    }

    @Override
    public boolean erOverstyrtPeriode() {
        return overstyrtPeriode != null;
    }

    @Override
    public LocalDate getTilOgMed() {
        return periode.getTomDato();
    }

    @Override
    public LocalDate getSisteLønnsendringsdato() {
        return sisteLønnsendringsdato;
    }

    @Override
    public boolean matcherPeriode(DatoIntervallEntitet aktivitetsAvtale) {
        return periode.equals(aktivitetsAvtale);
    }

    @Override
    public boolean getErLøpende() {
        return Tid.TIDENES_ENDE.equals(periode.getTomDato());
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

    public DatoIntervallEntitet getOverstyrtPeriode() {
        return overstyrtPeriode;
    }

    void setOverstyrtPeriode(DatoIntervallEntitet overstyrtPeriode) {
        this.overstyrtPeriode = overstyrtPeriode;
    }

    void sisteLønnsendringsdato(LocalDate sisteLønnsendringsdato) {
        this.sisteLønnsendringsdato = sisteLønnsendringsdato;
    }

    boolean skalMedEtterSkjæringstidspunktVurdering() {
        if (skjæringstidspunkt != null) {
            if (ventreSideAvSkjæringstidspunkt) {
                return periode.getFomDato().isBefore(skjæringstidspunkt);
            } else {
                return periode.getFomDato().isAfter(skjæringstidspunkt.minusDays(1)) ||
                    periode.getFomDato().isBefore(skjæringstidspunkt) && periode.getTomDato().isAfter(skjæringstidspunkt.minusDays(1));
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof AktivitetsAvtaleEntitet)) return false;
        var that = (AktivitetsAvtaleEntitet) o;
        return 
            Objects.equals(prosentsats, that.prosentsats) &&
            Objects.equals(periode, that.periode) &&
            Objects.equals(sisteLønnsendringsdato, that.sisteLønnsendringsdato);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prosentsats, periode, sisteLønnsendringsdato);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
            "periode=" + periode + //$NON-NLS-1$
            ", prosentsats=" + prosentsats + //$NON-NLS-1$
            '>';
    }

    boolean hasValues() {
        return prosentsats != null || periode != null;
    }

    @Override
    public boolean erAnsettelsesPeriode() {
        return (prosentsats == null || prosentsats.erNulltall())
            && sisteLønnsendringsdato == null;
    }

    void setSkjæringstidspunkt(LocalDate skjæringstidspunkt, boolean ventreSide) {
        this.skjæringstidspunkt = skjæringstidspunkt;
        this.ventreSideAvSkjæringstidspunkt = ventreSide;
    }

}
