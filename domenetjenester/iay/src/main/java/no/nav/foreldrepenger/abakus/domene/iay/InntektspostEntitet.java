package no.nav.foreldrepenger.abakus.domene.iay;

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

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.SkatteOgAvgiftsregelType;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "Inntektspost")
@Table(name = "IAY_INNTEKTSPOST")
public class InntektspostEntitet extends BaseEntitet implements Inntektspost, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INNTEKTSPOST")
    private Long id;

    /* TODO: splitt denne entiteten ? Kan ikke ha både inntektspostType og ytelseType satt samtidig (ene må være 'UDEFINERT'). Felter varier noe avh av hva som er satt*/
    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "inntektspost_type", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + InntektspostType.DISCRIMINATOR + "'"))})
    private InntektspostType inntektspostType;

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "skatte_og_avgiftsregel_type", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + SkatteOgAvgiftsregelType.DISCRIMINATOR + "'"))})
    private SkatteOgAvgiftsregelType skatteOgAvgiftsregelType = SkatteOgAvgiftsregelType.UDEFINERT;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inntekt_id", nullable = false, updatable = false, unique = true)
    private InntektEntitet inntekt;

    @Column(name = "kl_ytelse_type")
    private String ytelseType = OffentligYtelseType.DISCRIMINATOR;

    @ManyToOne
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "kl_ytelse_type" /* bruker kolonnenavn, da discriminator kan variere*/, referencedColumnName = "kodeverk")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "ytelse_type", referencedColumnName = "kode")),
    })
    private YtelseType ytelse = OffentligYtelseType.UDEFINERT;

    @Embedded
    private DatoIntervallEntitet periode;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "beloep", nullable = false)))
    @ChangeTracked
    private Beløp beløp;

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

    public InntektspostEntitet() {
        //hibernate
    }

    /**
     * Deep copy.
     */
    InntektspostEntitet(Inntektspost inntektspost) {
        this.inntektspostType = inntektspost.getInntektspostType();
        this.skatteOgAvgiftsregelType = inntektspost.getSkatteOgAvgiftsregelType();
        this.ytelse = inntektspost.getYtelseType();
        this.periode = DatoIntervallEntitet.fraOgMedTilOgMed(inntektspost.getFraOgMed(), inntektspost.getTilOgMed());
        this.beløp = inntektspost.getBeløp();
        this.ytelseType = inntektspost.getYtelseType().getKodeverk();
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(inntektspostType, ytelse, periode);
    }

    @Override
    public InntektspostType getInntektspostType() {
        return inntektspostType;
    }

    void setInntektspostType(InntektspostType inntektspostType) {
        this.inntektspostType = inntektspostType;
    }

    @Override
    public SkatteOgAvgiftsregelType getSkatteOgAvgiftsregelType() {
        return skatteOgAvgiftsregelType;
    }

    void setSkatteOgAvgiftsregelType(SkatteOgAvgiftsregelType skatteOgAvgiftsregelType) {
        this.skatteOgAvgiftsregelType = skatteOgAvgiftsregelType;
    }

    public void setPeriode(LocalDate fom, LocalDate tom) {
        this.periode = DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }

    @Override
    public LocalDate getFraOgMed() {
        return periode.getFomDato();
    }

    @Override
    public LocalDate getTilOgMed() {
        return periode.getTomDato();
    }

    @Override
    public Beløp getBeløp() {
        return beløp;
    }

    void setBeløp(Beløp beløp) {
        this.beløp = beløp;
    }

    @Override
    public YtelseType getYtelseType() {
        return ytelse;
    }

    public InntektEntitet getInntekt() {
        return inntekt;
    }

    void setInntekt(InntektEntitet inntekt) {
        this.inntekt = inntekt;
    }

    void setYtelse(YtelseType ytelse) {
        this.ytelseType = ytelse.getKodeverk();
        this.ytelse = ytelse;
    }

    
    /**
     * @deprecated FIXME - bør fjerne intern filtrering basert på initialisert transient skjæringstidspunkt.  Legg heller til egen Decorator klasse som filtrerer output fra entitet
     */
    @Deprecated
    boolean skalMedEtterSkjæringstidspunktVurdering() {
        if (skjæringstidspunkt != null) {
            if (ventreSideAvSkjæringstidspunkt) {
                return periode.getFomDato().isBefore(skjæringstidspunkt.plusDays(1));
            } else {
                return periode.getFomDato().isAfter(skjæringstidspunkt) ||
                    periode.getFomDato().isBefore(skjæringstidspunkt.plusDays(1)) && periode.getTomDato().isAfter(skjæringstidspunkt);
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof InntektspostEntitet)) {
            return false;
        }
        InntektspostEntitet other = (InntektspostEntitet) obj;
        return Objects.equals(this.getInntektspostType(), other.getInntektspostType())
            && Objects.equals(this.getYtelseType(), other.getYtelseType())
            && Objects.equals(this.getSkatteOgAvgiftsregelType(), other.getSkatteOgAvgiftsregelType())
            && Objects.equals(this.getFraOgMed(), other.getFraOgMed())
            && Objects.equals(this.getTilOgMed(), other.getTilOgMed());
    }

    @Override
    public int hashCode() {
        return Objects.hash(ytelseType, inntektspostType, periode, skatteOgAvgiftsregelType);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" +
            "ytelseType=" + ytelseType +
            "inntektspostType=" + inntektspostType +
            "skatteOgAvgiftsregelType=" + skatteOgAvgiftsregelType +
            ", fraOgMed=" + periode.getFomDato() +
            ", tilOgMed=" + periode.getTomDato() +
            ", beløp=" + beløp +
            '>';
    }

    public boolean hasValues() {
        return inntektspostType != null || periode.getFomDato() != null || periode.getTomDato() != null || beløp != null;
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
