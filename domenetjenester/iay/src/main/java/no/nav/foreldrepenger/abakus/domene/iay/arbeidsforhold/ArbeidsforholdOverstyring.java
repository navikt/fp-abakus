package no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.BekreftetPermisjon;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.BekreftetPermisjonStatus;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

/**
 * Overstyring av arbeidsforhold angitt av saksbehandler.
 */
@Entity(name = "ArbeidsforholdOverstyring")
@Table(name = "IAY_ARBEIDSFORHOLD")
public class ArbeidsforholdOverstyring extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_IAY_ARBEIDSFORHOLD")
    private Long id;

    @Embedded
    private Arbeidsgiver arbeidsgiver;

    @Embedded
    private InternArbeidsforholdRef arbeidsforholdRef;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "referanse", column = @Column(name = "arbeidsforhold_intern_id_ny", updatable = false)))
    private InternArbeidsforholdRef nyArbeidsforholdRef;

    @ChangeTracked
    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "handling_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + ArbeidsforholdHandlingType.DISCRIMINATOR + "'"))
    private ArbeidsforholdHandlingType handling = ArbeidsforholdHandlingType.UDEFINERT;

    @Column(name = "begrunnelse")
    private String begrunnelse;

    /**
     * Kjært navn for arbeidsgiver angitt av Saksbehandler (normalt kun ekstra arbeidsforhold lagt til). Ingen garanti for at dette matcher noe offisielt registrert navn.
     * 
     * Settes normalt kun for arbeidsforhold lagt til ekstra. Ellers hent fra
     * {@link no.nav.foreldrepenger.abakus.domene.iay.Yrkesaktivitet#getAktivitetsAvtalerForArbeid()}.
     */
    @Column(name = "arbeidsgiver_navn")
    private String arbeidsgiverNavn;

    /**
     * Stillingsprosent angitt av saksbehandler. 
     * 
     * Settes normalt kun for arbeidsforhold lagt til ekstra. Ellers hent fra
     * {@link no.nav.foreldrepenger.abakus.domene.iay.Yrkesaktivitet#getAktivitetsAvtalerForArbeid()}.
     */
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "stillingsprosent")))
    private Stillingsprosent stillingsprosent;

    @ManyToOne
    @JoinColumn(name = "informasjon_id", updatable = false, unique = true, nullable = false)
    private ArbeidsforholdInformasjonEntitet informasjon;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "arbeidsforholdOverstyring", cascade = CascadeType.PERSIST)
    private List<ArbeidsforholdOverstyrtePerioderEntitet> arbeidsforholdOverstyrtePerioder = new ArrayList<>();

    /**
     * Settes kun dersom saksbehandler har tatt stilling til permisjon. (om det skal brukes eller ikke). Bruk ellers
     * {@link no.nav.foreldrepenger.abakus.domene.iay.Yrkesaktivitet#getPermisjon()}.
     */
    @Embedded
    private BekreftetPermisjon bekreftetPermisjon = new BekreftetPermisjon();

    ArbeidsforholdOverstyring() {
    }

    ArbeidsforholdOverstyring(ArbeidsforholdOverstyring kopierFra) {
        this.arbeidsgiver = kopierFra.getArbeidsgiver();
        this.arbeidsforholdRef = kopierFra.getArbeidsforholdRef();
        this.handling = kopierFra.getHandling();
        this.nyArbeidsforholdRef = kopierFra.getNyArbeidsforholdRef();
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(arbeidsgiver, arbeidsforholdRef);
    }

    void setInformasjon(ArbeidsforholdInformasjonEntitet arbeidsforholdInformasjonEntitet) {
        this.informasjon = arbeidsforholdInformasjonEntitet;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    void setArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    public InternArbeidsforholdRef getArbeidsforholdRef() {
        return arbeidsforholdRef != null ? arbeidsforholdRef : InternArbeidsforholdRef.nullRef();
    }

    void setArbeidsforholdRef(InternArbeidsforholdRef arbeidsforholdRef) {
        this.arbeidsforholdRef = arbeidsforholdRef;
    }

    public ArbeidsforholdHandlingType getHandling() {
        return handling;
    }

    void setHandling(ArbeidsforholdHandlingType handling) {
        this.handling = handling;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public void setBeskrivelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    public void leggTilOverstyrtPeriode(LocalDate fom, LocalDate tom) {
        ArbeidsforholdOverstyrtePerioderEntitet overstyrtPeriode = new ArbeidsforholdOverstyrtePerioderEntitet();
        overstyrtPeriode.setPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom));
        overstyrtPeriode.setArbeidsforholdOverstyring(this);
        arbeidsforholdOverstyrtePerioder.add(overstyrtPeriode);
    }

    public List<ArbeidsforholdOverstyrtePerioderEntitet> getArbeidsforholdOverstyrtePerioder() {
        return arbeidsforholdOverstyrtePerioder;
    }

    public InternArbeidsforholdRef getNyArbeidsforholdRef() {
        return nyArbeidsforholdRef;
    }

    void setNyArbeidsforholdRef(InternArbeidsforholdRef nyArbeidsforholdRef) {
        this.nyArbeidsforholdRef = nyArbeidsforholdRef;
    }
    
    public String getArbeidsgiverNavn() {
        return arbeidsgiverNavn;
    }
    
    public Stillingsprosent getStillingsprosent() {
        return stillingsprosent;
    }
    
    public Optional<BekreftetPermisjon> getBekreftetPermisjon() {
        if (bekreftetPermisjon.getStatus().equals(BekreftetPermisjonStatus.UDEFINERT)){
            return Optional.empty();
        }
        return Optional.of(bekreftetPermisjon);
    }
    
    void setBekreftetPermisjon(BekreftetPermisjon bekreftetPermisjon) {
        this.bekreftetPermisjon = bekreftetPermisjon;
    }
    
    public boolean erOverstyrt(){
        return !Objects.equals(ArbeidsforholdHandlingType.BRUK, handling)
            || ( Objects.equals(ArbeidsforholdHandlingType.BRUK, handling) &&
            !Objects.equals(bekreftetPermisjon.getStatus(), BekreftetPermisjonStatus.UDEFINERT) );
    }

    @SuppressWarnings("deprecation")
    public boolean kreverIkkeInntektsmelding() {
        return Set.of(ArbeidsforholdHandlingType.LAGT_TIL_AV_SAKSBEHANDLER, 
            ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING,
            ArbeidsforholdHandlingType.BRUK_MED_OVERSTYRT_PERIODE, 
            ArbeidsforholdHandlingType.INNTEKT_IKKE_MED_I_BG).contains(handling);
    }
    
    void setArbeidsgiverNavn(String arbeidsgiverNavn) {
        this.arbeidsgiverNavn = arbeidsgiverNavn;
    }
    
    void setStillingsprosent(Stillingsprosent stillingsprosent) {
        this.stillingsprosent = stillingsprosent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null ||!(o instanceof ArbeidsforholdOverstyring))
            return false;
        var that = (ArbeidsforholdOverstyring) o;
        return Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
            Objects.equals(arbeidsforholdRef, that.arbeidsforholdRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiver, arbeidsforholdRef);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+
            "<arbeidsgiver=" + arbeidsgiver +
            ", arbeidsforholdRef=" + arbeidsforholdRef +
            ", handling=" + handling +
            '>';
    }
}
