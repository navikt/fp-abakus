package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDate;
import java.util.Objects;

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

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.abakus.iaygrunnlag.kodeverk.SkatteOgAvgiftsregelType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.SkatteOgAvgiftsregelTypeKodeverdiConverter;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.Beløp;

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

    @Convert(converter = SkatteOgAvgiftsregelTypeKodeverdiConverter.class)
    @Column(name = "skatte_og_avgiftsregel_type", nullable = false, updatable = false)
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
    private YtelseInntektspostType ytelse = OffentligYtelseType.UDEFINERT;

    @Embedded
    private IntervallEntitet periode;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "beloep", nullable = false)))
    @ChangeTracked
    private Beløp beløp;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

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
        this.periode = inntektspost.getPeriode();
        this.beløp = inntektspost.getBeløp();
        this.ytelseType = inntektspost.getYtelseType().getKodeverk();
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = { inntektspostType, ytelse, periode };
        return IndexKeyComposer.createKey(keyParts);
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
        this.periode = IntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }

    @Override
    public Beløp getBeløp() {
        return beløp;
    }
    
    @Override
    public IntervallEntitet getPeriode() {
        return periode;
    }
    
    void setBeløp(Beløp beløp) {
        this.beløp = beløp;
    }

    @Override
    public YtelseInntektspostType getYtelseType() {
        return ytelse;
    }

    public InntektEntitet getInntekt() {
        return inntekt;
    }

    void setInntekt(InntektEntitet inntekt) {
        this.inntekt = inntekt;
    }

    void setYtelse(YtelseInntektspostType ytelse) {
        this.ytelseType = ytelse.getKodeverk();
        this.ytelse = ytelse;
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
            && Objects.equals(this.getPeriode(), other.getPeriode());
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

}
