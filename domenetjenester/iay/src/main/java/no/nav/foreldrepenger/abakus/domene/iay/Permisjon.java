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

import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.abakus.iaygrunnlag.kodeverk.PermisjonsbeskrivelseType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.PermisjonsbeskrivelseTypeKodeverdiConverter;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;

@Entity(name = "Permisjon")
@Table(name = "IAY_PERMISJON")
public class Permisjon extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PERMISJON")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "yrkesaktivitet_id", nullable = false, updatable = false, unique = true)
    private Yrkesaktivitet yrkesaktivitet;

    @Convert(converter = PermisjonsbeskrivelseTypeKodeverdiConverter.class)
    @Column(name = "beskrivelse_type", nullable = false, updatable = false)
    private PermisjonsbeskrivelseType permisjonsbeskrivelseType;

    @Embedded
    private IntervallEntitet periode;

    @ChangeTracked
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "prosentsats")))
    private Stillingsprosent prosentsats;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public Permisjon() {
        // hibernate
    }

    /**
     * Deep copy ctor
     */
    Permisjon(Permisjon permisjon) {
        this.permisjonsbeskrivelseType = permisjon.getPermisjonsbeskrivelseType();
        this.periode = IntervallEntitet.fraOgMedTilOgMed(permisjon.getFraOgMed(), permisjon.getTilOgMed());
        this.prosentsats = permisjon.getProsentsats();
    }

    @Override
    public String getIndexKey() {
        return IndexKeyComposer.createKey(periode, getPermisjonsbeskrivelseType());
    }

    /**
     * Beskrivelse av permisjonen
     *
     * @return {@link PermisjonsbeskrivelseType}
     */
    public PermisjonsbeskrivelseType getPermisjonsbeskrivelseType() {
        return permisjonsbeskrivelseType;
    }

    public void setPermisjonsbeskrivelseType(PermisjonsbeskrivelseType permisjonsbeskrivelseType) {
        this.permisjonsbeskrivelseType = permisjonsbeskrivelseType;
    }

    public void setPeriode(LocalDate fraOgMed, LocalDate tilOgMed) {
        if (tilOgMed != null) {
            this.periode = IntervallEntitet.fraOgMedTilOgMed(fraOgMed, tilOgMed);
        } else {
            this.periode = IntervallEntitet.fraOgMed(fraOgMed);
        }
    }

    /**
     * Periode start
     *
     * @return første dag i perioden
     */
    public LocalDate getFraOgMed() {
        return periode.getFomDato();
    }

    /**
     * Periode slutt
     *
     * @return siste dag i perioden
     */
    public LocalDate getTilOgMed() {
        return periode.getTomDato();
    }

    /**
     * Prosentsats som aktøren er permitert fra arbeidet
     *
     * @return prosentsats
     */
    public Stillingsprosent getProsentsats() {
        return prosentsats;
    }

    public void setProsentsats(Stillingsprosent prosentsats) {
        this.prosentsats = prosentsats;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Permisjon)) {
            return false;
        }
        Permisjon other = (Permisjon) obj;
        return Objects.equals(this.getPermisjonsbeskrivelseType(), other.getPermisjonsbeskrivelseType())
            && Objects.equals(this.getFraOgMed(), other.getFraOgMed())
            && Objects.equals(this.getTilOgMed(), other.getTilOgMed());
    }

    @Override
    public int hashCode() {
        return Objects.hash(permisjonsbeskrivelseType, periode);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
            "permisjonsbeskrivelseType=" + permisjonsbeskrivelseType + //$NON-NLS-1$
            ", fraOgMed=" + periode.getFomDato() + //$NON-NLS-1$
            ", tilOgMed=" + periode.getTomDato() + //$NON-NLS-1$
            ", v=" + prosentsats + //$NON-NLS-1$
            '>';
    }

    public Yrkesaktivitet getYrkesaktivitet() {
        return yrkesaktivitet;
    }

    void setYrkesaktivitet(Yrkesaktivitet yrkesaktivitet) {
        this.yrkesaktivitet = yrkesaktivitet;
    }

    boolean hasValues() {
        return permisjonsbeskrivelseType != null || periode.getFomDato() != null || prosentsats != null;
    }
}
