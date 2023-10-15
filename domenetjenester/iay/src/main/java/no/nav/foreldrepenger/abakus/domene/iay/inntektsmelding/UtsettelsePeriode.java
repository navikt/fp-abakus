package no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding;

import java.time.LocalDate;
import java.util.Objects;

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
import no.nav.abakus.iaygrunnlag.kodeverk.UtsettelseÅrsakType;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.iay.jpa.UtsettelseÅrsakTypeKodeverdiConverter;

@Entity(name = "UtsettelsePeriode")
@Table(name = "IAY_UTSETTELSE_PERIODE")
public class UtsettelsePeriode extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_UTSETTELSE_PERIODE")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inntektsmelding_id", nullable = false, updatable = false)
    private Inntektsmelding inntektsmelding;

    @Embedded
    @ChangeTracked
    private IntervallEntitet periode;

    @ChangeTracked
    @Convert(converter = UtsettelseÅrsakTypeKodeverdiConverter.class)
    @Column(name = "utsettelse_aarsak_type", nullable = false, updatable = false)
    private UtsettelseÅrsakType årsak = UtsettelseÅrsakType.UDEFINERT;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    private UtsettelsePeriode(LocalDate fom, LocalDate tom) {
        this.periode = IntervallEntitet.fraOgMedTilOgMed(fom, tom);
        this.årsak = UtsettelseÅrsakType.FERIE;
    }

    private UtsettelsePeriode(LocalDate fom, LocalDate tom, UtsettelseÅrsakType årsak) {
        this.årsak = årsak;
        this.periode = IntervallEntitet.fraOgMedTilOgMed(fom, tom);
    }

    UtsettelsePeriode() {
    }

    UtsettelsePeriode(UtsettelsePeriode utsettelsePeriode) {
        this.periode = utsettelsePeriode.getPeriode();
        this.årsak = utsettelsePeriode.getÅrsak();
    }

    public static UtsettelsePeriode ferie(LocalDate fom, LocalDate tom) {
        return new UtsettelsePeriode(fom, tom);
    }

    public static UtsettelsePeriode utsettelse(LocalDate fom, LocalDate tom, UtsettelseÅrsakType årsak) {
        return new UtsettelsePeriode(fom, tom, årsak);
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {årsak, periode};
        return IndexKeyComposer.createKey(keyParts);
    }

    /**
     * Perioden som utsettes
     *
     * @return perioden
     */
    public IntervallEntitet getPeriode() {
        return periode;
    }

    /**
     * Årsaken til utsettelsen
     *
     * @return utsettelseårsaken
     */
    public UtsettelseÅrsakType getÅrsak() {
        return årsak;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof UtsettelsePeriode)) {
            return false;
        }
        var that = (UtsettelsePeriode) o;
        return Objects.equals(periode, that.periode) && Objects.equals(årsak, that.årsak);
    }

    @Override
    public int hashCode() {
        return Objects.hash(periode, årsak);
    }

    @Override
    public String toString() {
        return "UtsettelsePeriodeEntitet{" + "id=" + id + ", periode=" + periode + ", årsak=" + årsak + '}';
    }

    void setInntektsmelding(Inntektsmelding inntektsmelding) {
        this.inntektsmelding = inntektsmelding;
    }
}
