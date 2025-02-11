package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektYtelseType;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.abakus.iaygrunnlag.kodeverk.LønnsinntektBeskrivelse;
import no.nav.abakus.iaygrunnlag.kodeverk.SkatteOgAvgiftsregelType;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.iay.jpa.InntektYtelseTypeKodeverdiConverter;
import no.nav.foreldrepenger.abakus.iay.jpa.InntektspostTypeKodeverdiConverter;
import no.nav.foreldrepenger.abakus.iay.jpa.LønnsbeskrivelseKodeverdiConverter;
import no.nav.foreldrepenger.abakus.iay.jpa.SkatteOgAvgiftsregelTypeKodeverdiConverter;
import no.nav.foreldrepenger.abakus.typer.Beløp;

@Entity(name = "Inntektspost")
@Table(name = "IAY_INNTEKTSPOST")
public class Inntektspost extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INNTEKTSPOST")
    private Long id;

    @Convert(converter = InntektspostTypeKodeverdiConverter.class)
    @Column(name = "inntektspost_type", nullable = false, updatable = false)
    private InntektspostType inntektspostType;

    @Convert(converter = SkatteOgAvgiftsregelTypeKodeverdiConverter.class)
    @Column(name = "skatte_og_avgiftsregel_type", nullable = false, updatable = false)
    private SkatteOgAvgiftsregelType skatteOgAvgiftsregelType = SkatteOgAvgiftsregelType.UDEFINERT;

    /**
     * Beskriver hva inntekten gjelder dersom den er av typen LØNN
     */
    @Convert(converter = LønnsbeskrivelseKodeverdiConverter.class)
    @Column(name = "lonnsinntekt_beskrivelse", nullable = false, updatable = false)
    private LønnsinntektBeskrivelse lønnsinntektBeskrivelse = LønnsinntektBeskrivelse.UDEFINERT;
    @ManyToOne(optional = false)
    @JoinColumn(name = "inntekt_id", nullable = false, updatable = false, unique = true)
    private Inntekt inntekt;

    @Convert(converter = InntektYtelseTypeKodeverdiConverter.class)
    @Column(name = "ytelse_type", updatable = false)
    private InntektYtelseType ytelse;

    @Embedded
    private IntervallEntitet periode;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "beloep", nullable = false)))
    @ChangeTracked
    private Beløp beløp;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public Inntektspost() {
        // hibernate
    }

    /**
     * Deep copy.
     */
    Inntektspost(Inntektspost inntektspost) {
        this.inntektspostType = inntektspost.getInntektspostType();
        this.skatteOgAvgiftsregelType = inntektspost.getSkatteOgAvgiftsregelType();
        this.lønnsinntektBeskrivelse = inntektspost.getLønnsinntektBeskrivelse();
        this.periode = inntektspost.getPeriode();
        this.beløp = inntektspost.getBeløp();
        this.ytelse = inntektspost.getInntektYtelseType();
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {inntektspostType, ytelse, periode};
        return IndexKeyComposer.createKey(keyParts);
    }

    /**
     * Underkategori av utbetaling
     * <p>
     * F.eks
     * <ul>
     * <li>Lønn</li>
     * <li>Ytelse</li>
     * <li>Næringsinntekt</li>
     * </ul>
     *
     * @return {@link InntektspostType}
     */
    public InntektspostType getInntektspostType() {
        return inntektspostType;
    }

    void setInntektspostType(InntektspostType inntektspostType) {
        this.inntektspostType = inntektspostType;
    }

    /**
     * En kodeverksverdi som angir særskilt beskatningsregel.
     * Den er ikke alltid satt, og kommer fra inntektskomponenten
     *
     * @return {@link SkatteOgAvgiftsregelType}
     */
    public SkatteOgAvgiftsregelType getSkatteOgAvgiftsregelType() {
        return skatteOgAvgiftsregelType;
    }

    void setSkatteOgAvgiftsregelType(SkatteOgAvgiftsregelType skatteOgAvgiftsregelType) {
        this.skatteOgAvgiftsregelType = skatteOgAvgiftsregelType;
    }

    public LønnsinntektBeskrivelse getLønnsinntektBeskrivelse() {
        return lønnsinntektBeskrivelse;
    }

    public void setLønnsinntektBeskrivelse(LønnsinntektBeskrivelse lønnsinntektBeskrivelse) {
        this.lønnsinntektBeskrivelse = lønnsinntektBeskrivelse;
    }

    public void setPeriode(LocalDate fom, LocalDate tom) {
        this.periode = IntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }

    /**
     * Beløpet som har blitt utbetalt i perioden
     *
     * @return Beløpet
     */
    public Beløp getBeløp() {
        return beløp;
    }

    void setBeløp(Beløp beløp) {
        this.beløp = beløp;
    }

    /**
     * Periode inntektsposten gjelder.
     *
     * @return
     */
    public IntervallEntitet getPeriode() {
        return periode;
    }

    public Inntekt getInntekt() {
        return inntekt;
    }

    void setInntekt(Inntekt inntekt) {
        this.inntekt = inntekt;
    }

    public InntektYtelseType getInntektYtelseType() {
        return ytelse;
    }

    void setYtelse(InntektYtelseType ytelse) {
        this.ytelse = ytelse; // innfører null. migrerer gamle "-"
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Inntektspost)) {
            return false;
        }
        Inntektspost other = (Inntektspost) obj;
        return Objects.equals(this.inntektspostType, other.inntektspostType) && Objects.equals(this.ytelse, other.ytelse) && Objects.equals(
            this.skatteOgAvgiftsregelType, other.skatteOgAvgiftsregelType) && Objects.equals(this.periode, other.periode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inntektspostType, ytelse, skatteOgAvgiftsregelType, periode);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + ", ytelse=" + ytelse + ", inntektspostType=" + inntektspostType + ", skatteOgAvgiftsregelType="
            + skatteOgAvgiftsregelType + ", periode=" + periode + ", beløp=" + beløp + '>';
    }

    public boolean hasValues() {
        return (ytelse != null || !Objects.equals(ytelse, "-")) || inntektspostType != null || periode.getFomDato() != null
            || periode.getTomDato() != null || beløp != null;
    }

}
