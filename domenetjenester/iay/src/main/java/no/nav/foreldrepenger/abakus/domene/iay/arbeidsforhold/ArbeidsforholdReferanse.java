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

import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.diff.TraverseValue;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;

@Entity(name = "ArbeidsforholdReferanse")
@Table(name = "IAY_ARBEIDSFORHOLD_REFER")
public class ArbeidsforholdReferanse extends BaseEntitet implements IndexKey, TraverseValue {

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
    private InternArbeidsforholdRef internReferanse;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "referanse", column = @Column(name = "ekstern_referanse", nullable = false))
    })
    private EksternArbeidsforholdRef eksternReferanse;

    @ManyToOne
    @JoinColumn(name = "informasjon_id", updatable = false, unique = true, nullable = false)
    private ArbeidsforholdInformasjon informasjon;

    ArbeidsforholdReferanse() {
    }

    public ArbeidsforholdReferanse(Arbeidsgiver arbeidsgiverEntitet, InternArbeidsforholdRef internReferanse, EksternArbeidsforholdRef eksternReferanse) {
        this.arbeidsgiverEntitet = arbeidsgiverEntitet;
        this.internReferanse = internReferanse;
        this.eksternReferanse = eksternReferanse;
    }

    ArbeidsforholdReferanse(ArbeidsforholdReferanse arbeidsforholdInformasjonEntitet) {
        this(arbeidsforholdInformasjonEntitet.arbeidsgiverEntitet, arbeidsforholdInformasjonEntitet.internReferanse,
            arbeidsforholdInformasjonEntitet.eksternReferanse);
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = { internReferanse, eksternReferanse };
        return IndexKeyComposer.createKey(keyParts);
    }

    public InternArbeidsforholdRef getInternReferanse() {
        return internReferanse;
    }

    public EksternArbeidsforholdRef getEksternReferanse() {
        return eksternReferanse;
    }
    
    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiverEntitet;
    }

    void setInformasjon(ArbeidsforholdInformasjon informasjon) {
        this.informasjon = informasjon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof ArbeidsforholdReferanse))
            return false;
        var that = (ArbeidsforholdReferanse) o;
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
