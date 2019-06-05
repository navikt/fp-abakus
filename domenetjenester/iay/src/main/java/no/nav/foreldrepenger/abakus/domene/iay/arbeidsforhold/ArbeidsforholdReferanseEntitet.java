package no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold;

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

import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.felles.diff.TraverseValue;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;

@Entity(name = "ArbeidsforholdReferanse")
@Table(name = "IAY_ARBEIDSFORHOLD_REFER")
public class ArbeidsforholdReferanseEntitet extends BaseEntitet implements IndexKey, TraverseValue {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_IAY_ARBEIDSFORHOLD_REFER")
    private Long id;

    @ChangeTracked
    @Embedded
    private Arbeidsgiver arbeidsgiverEntitet;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "referanse", column = @Column(name = "intern_referanse", nullable = false))
    })
    private ArbeidsforholdRef internReferanse;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "referanse", column = @Column(name = "ekstern_referanse", nullable = false))
    })
    private ArbeidsforholdRef eksternReferanse;

    @ManyToOne
    @JoinColumn(name = "informasjon_id", updatable = false, unique = true, nullable = false)
    private ArbeidsforholdInformasjonEntitet informasjon;

    ArbeidsforholdReferanseEntitet() {
    }

    /**
     * @deprecated Bruk {@link #ArbeidsforholdReferanseEntitet(Arbeidsgiver, InternArbeidsforholdRef, EksternArbeidsforholdRef)}.
     */
    @Deprecated(forRemoval = true)
    ArbeidsforholdReferanseEntitet(Arbeidsgiver arbeidsgiverEntitet, ArbeidsforholdRef internReferanse, ArbeidsforholdRef eksternReferanse) {
        this.arbeidsgiverEntitet = arbeidsgiverEntitet;
        this.internReferanse = internReferanse;
        this.eksternReferanse = eksternReferanse;
    }

    public ArbeidsforholdReferanseEntitet(Arbeidsgiver arbeidsgiverEntitet, InternArbeidsforholdRef internReferanse, EksternArbeidsforholdRef eksternReferanse) {
        this.arbeidsgiverEntitet = arbeidsgiverEntitet;
        this.internReferanse = internReferanse == null ? null : ArbeidsforholdRef.ref(internReferanse.getReferanse());
        this.eksternReferanse = eksternReferanse == null ? null : ArbeidsforholdRef.ref(eksternReferanse.getReferanse());
    }

    ArbeidsforholdReferanseEntitet(ArbeidsforholdReferanseEntitet arbeidsforholdInformasjonEntitet) {
        this(arbeidsforholdInformasjonEntitet.arbeidsgiverEntitet, arbeidsforholdInformasjonEntitet.internReferanse,
            arbeidsforholdInformasjonEntitet.eksternReferanse);
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(internReferanse, eksternReferanse);
    }

    public ArbeidsforholdRef getInternReferanse() {
        return internReferanse;
    }

    public ArbeidsforholdRef getEksternReferanse() {
        return eksternReferanse;
    }
    
    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiverEntitet;
    }

    void setInformasjon(ArbeidsforholdInformasjonEntitet informasjon) {
        this.informasjon = informasjon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ArbeidsforholdReferanseEntitet that = (ArbeidsforholdReferanseEntitet) o;
        return Objects.equals(arbeidsgiverEntitet, that.arbeidsgiverEntitet) &&
            Objects.equals(internReferanse, that.internReferanse) &&
            Objects.equals(eksternReferanse, that.eksternReferanse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiverEntitet, internReferanse, eksternReferanse);
    }

    @Override
    public String toString() {
        return "ArbeidsforholdReferanseEntitet{" +
            "ArbeidsgiverEntitet=" + arbeidsgiverEntitet +
            ", internReferanse=" + internReferanse +
            ", eksternReferanse=" + eksternReferanse +
            '}';
    }
}
