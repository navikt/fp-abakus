package no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold;

import jakarta.persistence.*;
import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidsforholdHandlingType;
import no.nav.abakus.iaygrunnlag.kodeverk.BekreftetPermisjonStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.BekreftetPermisjon;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.iay.jpa.ArbeidsforholdHandlingTypeKodeverdiConverter;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
    @Convert(converter = ArbeidsforholdHandlingTypeKodeverdiConverter.class)
    @Column(name = "handling_type", nullable = false, updatable = false)
    private ArbeidsforholdHandlingType handling = ArbeidsforholdHandlingType.UDEFINERT;

    @Column(name = "begrunnelse")
    private String begrunnelse;

    /**
     * Kjært navn for arbeidsgiver angitt av Saksbehandler (normalt kun ekstra arbeidsforhold lagt til). Ingen garanti for at dette matcher noe offisielt registrert navn.
     * <p>
     * Settes normalt kun for arbeidsforhold lagt til ekstra. Ellers hent fra
     * {@link no.nav.foreldrepenger.abakus.domene.iay.Yrkesaktivitet#getAktivitetsAvtalerForArbeid()}.
     */
    @Column(name = "arbeidsgiver_navn")
    private String arbeidsgiverNavn;

    /**
     * Stillingsprosent angitt av saksbehandler.
     * <p>
     * Settes normalt kun for arbeidsforhold lagt til ekstra. Ellers hent fra
     * {@link no.nav.foreldrepenger.abakus.domene.iay.Yrkesaktivitet#getAktivitetsAvtalerForArbeid()}.
     */
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "stillingsprosent")))
    private Stillingsprosent stillingsprosent;

    @ManyToOne
    @JoinColumn(name = "informasjon_id", updatable = false, unique = true, nullable = false)
    private ArbeidsforholdInformasjon informasjon;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "arbeidsforholdOverstyring", cascade = CascadeType.PERSIST)
    private List<ArbeidsforholdOverstyrtePerioder> arbeidsforholdOverstyrtePerioder = new ArrayList<>();

    /**
     * Settes kun dersom saksbehandler har tatt stilling til permisjon. (om det skal brukes eller ikke). Bruk ellers
     * {@link no.nav.foreldrepenger.abakus.domene.iay.Yrkesaktivitet#getPermisjon()}.
     */
    @Embedded
    private BekreftetPermisjon bekreftetPermisjon = new BekreftetPermisjon();

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    ArbeidsforholdOverstyring() {
        // Plattform (CDI, Hibernate, Jackson)
    }

    ArbeidsforholdOverstyring(ArbeidsforholdOverstyring kopierFra) {
        this.arbeidsgiver = kopierFra.getArbeidsgiver();
        this.arbeidsforholdRef = kopierFra.getArbeidsforholdRef();
        this.handling = kopierFra.getHandling();
        this.stillingsprosent = kopierFra.getStillingsprosent();
        this.nyArbeidsforholdRef = kopierFra.getNyArbeidsforholdRef();
        this.arbeidsgiverNavn = kopierFra.getArbeidsgiverNavn();
        this.begrunnelse = kopierFra.getBegrunnelse();
        this.arbeidsforholdOverstyrtePerioder = kopierFra.getArbeidsforholdOverstyrtePerioder()
            .stream()
            .map(ArbeidsforholdOverstyrtePerioder::new)
            .peek(it -> it.setArbeidsforholdOverstyring(this))
            .collect(Collectors.toList());
        this.bekreftetPermisjon = kopierFra.bekreftetPermisjon;
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {arbeidsgiver, arbeidsforholdRef};
        return IndexKeyComposer.createKey(keyParts);
    }

    void setInformasjon(ArbeidsforholdInformasjon arbeidsforholdInformasjonEntitet) {
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
        if (handling.isReadOnly()) {
            throw new UnsupportedOperationException("Kan ikke opprette grunnlag da ArbeidsforholdHandlingType ikke lenger er supportert (annet enn lesing: " + handling);
        }
        this.handling = handling;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public void setBeskrivelse(String begrunnelse) {
        this.begrunnelse = begrunnelse;
    }

    public void leggTilOverstyrtPeriode(LocalDate fom, LocalDate tom) {
        ArbeidsforholdOverstyrtePerioder overstyrtPeriode = new ArbeidsforholdOverstyrtePerioder();
        overstyrtPeriode.setPeriode(IntervallEntitet.fraOgMedTilOgMed(fom, tom));
        overstyrtPeriode.setArbeidsforholdOverstyring(this);
        arbeidsforholdOverstyrtePerioder.add(overstyrtPeriode);
    }

    public List<ArbeidsforholdOverstyrtePerioder> getArbeidsforholdOverstyrtePerioder() {
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

    void setArbeidsgiverNavn(String arbeidsgiverNavn) {
        this.arbeidsgiverNavn = arbeidsgiverNavn;
    }

    public Stillingsprosent getStillingsprosent() {
        return stillingsprosent;
    }

    void setStillingsprosent(Stillingsprosent stillingsprosent) {
        this.stillingsprosent = stillingsprosent;
    }

    public Optional<BekreftetPermisjon> getBekreftetPermisjon() {
        if (bekreftetPermisjon.getStatus().equals(BekreftetPermisjonStatus.UDEFINERT)) {
            return Optional.empty();
        }
        return Optional.of(bekreftetPermisjon);
    }

    void setBekreftetPermisjon(BekreftetPermisjon bekreftetPermisjon) {
        this.bekreftetPermisjon = bekreftetPermisjon;
    }

    public boolean erOverstyrt() {
        return !Objects.equals(ArbeidsforholdHandlingType.BRUK, handling) || (Objects.equals(ArbeidsforholdHandlingType.BRUK, handling)
            && !Objects.equals(bekreftetPermisjon.getStatus(), BekreftetPermisjonStatus.UDEFINERT));
    }

    public boolean kreverIkkeInntektsmelding() {
        return Set.of(ArbeidsforholdHandlingType.LAGT_TIL_AV_SAKSBEHANDLER, ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING,
            ArbeidsforholdHandlingType.BRUK_MED_OVERSTYRT_PERIODE, ArbeidsforholdHandlingType.INNTEKT_IKKE_MED_I_BG).contains(handling);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof ArbeidsforholdOverstyring)) {
            return false;
        }
        var that = (ArbeidsforholdOverstyring) o;
        return Objects.equals(arbeidsgiver, that.arbeidsgiver) && Objects.equals(arbeidsforholdRef, that.arbeidsforholdRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiver, arbeidsforholdRef);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<arbeidsgiver=" + arbeidsgiver + ", arbeidsforholdRef=" + arbeidsforholdRef + ", handling=" + handling
            + '>';
    }
}
