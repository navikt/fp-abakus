package no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.abakus.domene.iay.InntektsmeldingAggregatEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsmeldingInnsendingsårsak;
import no.nav.foreldrepenger.abakus.domene.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "Inntektsmelding")
@Table(name = "IAY_INNTEKTSMELDING")
public class InntektsmeldingEntitet extends BaseEntitet implements Inntektsmelding, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INNTEKTSMELDING")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inntektsmeldinger_id", nullable = false, updatable = false)
    private InntektsmeldingAggregatEntitet inntektsmeldinger;

    @OneToMany(mappedBy = "inntektsmelding")
    @ChangeTracked
    private List<GraderingEntitet> graderinger = new ArrayList<>();

    @OneToMany(mappedBy = "inntektsmelding")
    @ChangeTracked
    private List<NaturalYtelseEntitet> naturalYtelser = new ArrayList<>();

    @OneToMany(mappedBy = "inntektsmelding")
    @ChangeTracked
    private List<UtsettelsePeriodeEntitet> utsettelsePerioder = new ArrayList<>();

    @Embedded
    @ChangeTracked
    private Arbeidsgiver arbeidsgiver;

    @Embedded
    @ChangeTracked
    private ArbeidsforholdRef arbeidsforholdRef;

    @Column(name = "start_dato_permisjon", updatable = false, nullable = false)
    @ChangeTracked
    private LocalDate startDatoPermisjon;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "naer_relasjon", updatable = false, nullable = false)
    private boolean nærRelasjon;

    @Column(name = "mottatt_dokument_id", updatable = false, nullable = false)
    private Long mottattDokumentId;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "inntekt_beloep", nullable = false)))
    @ChangeTracked
    private Beløp inntektBeløp;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "refusjon_beloep", updatable = false)))
    @ChangeTracked
    private Beløp refusjonBeløpPerMnd;

    @Column(name = "refusjon_opphoerer", updatable = false)
    @ChangeTracked
    private LocalDate refusjonOpphører;

    @Column(name = "innsendingstidspunkt", updatable = false, nullable = false)
    private LocalDateTime innsendingstidspunkt;

    @Column(name = "kanalreferanse")
    private String kanalreferanse;

    @Column(name = "kildesystem")
    private String kildesystem;

    @OneToMany(mappedBy = "inntektsmelding")
    @ChangeTracked
    private List<RefusjonEntitet> endringerRefusjon = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "innsendingsaarsak", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + InntektsmeldingInnsendingsårsak.DISCRIMINATOR
            + "'"))})


    @ChangeTracked
    private InntektsmeldingInnsendingsårsak innsendingsårsak = InntektsmeldingInnsendingsårsak.UDEFINERT;

    InntektsmeldingEntitet() {
    }

    public InntektsmeldingEntitet(Inntektsmelding inntektsmelding) {
        this.arbeidsgiver = inntektsmelding.getArbeidsgiver();
        this.arbeidsforholdRef = inntektsmelding.getArbeidsforholdRef();
        this.startDatoPermisjon = inntektsmelding.getStartDatoPermisjon();
        this.nærRelasjon = inntektsmelding.getErNærRelasjon();
        this.mottattDokumentId = inntektsmelding.getMottattDokumentId();
        this.inntektBeløp = inntektsmelding.getInntektBeløp();
        this.refusjonBeløpPerMnd = inntektsmelding.getRefusjonBeløpPerMnd();
        this.refusjonOpphører = inntektsmelding.getRefusjonOpphører();
        this.innsendingsårsak = inntektsmelding.getInntektsmeldingInnsendingsårsak();
        this.innsendingstidspunkt = inntektsmelding.getInnsendingstidspunkt();
        this.kanalreferanse = inntektsmelding.getKanalreferanse();
        this.kildesystem = inntektsmelding.getKildesystem();
        this.graderinger = inntektsmelding.getGraderinger().stream().map(g -> {
            final GraderingEntitet graderingEntitet = new GraderingEntitet(g);
            graderingEntitet.setInntektsmelding(this);
            return graderingEntitet;
        }).collect(Collectors.toList());
        this.naturalYtelser = inntektsmelding.getNaturalYtelser().stream().map(n -> {
            final NaturalYtelseEntitet naturalYtelseEntitet = new NaturalYtelseEntitet(n);
            naturalYtelseEntitet.setInntektsmelding(this);
            return naturalYtelseEntitet;
        }).collect(Collectors.toList());
        this.utsettelsePerioder = inntektsmelding.getUtsettelsePerioder().stream().map(u -> {
            final UtsettelsePeriodeEntitet utsettelsePeriodeEntitet = new UtsettelsePeriodeEntitet(u);
            utsettelsePeriodeEntitet.setInntektsmelding(this);
            return utsettelsePeriodeEntitet;
        }).collect(Collectors.toList());
        this.endringerRefusjon = inntektsmelding.getEndringerRefusjon().stream().map(r -> {
            final RefusjonEntitet refusjonEntitet = new RefusjonEntitet(r);
            refusjonEntitet.setInntektsmelding(this);
            return refusjonEntitet;
        }).collect(Collectors.toList());
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(arbeidsgiver, arbeidsforholdRef);
    }

    @Override
    public Long getMottattDokumentId() {
        return mottattDokumentId;
    }

    void setMottattDokumentId(Long mottattDokumentId) {
        this.mottattDokumentId = mottattDokumentId;
    }

    public void setInntektsmeldinger(InntektsmeldingAggregatEntitet inntektsmeldinger) {
        this.inntektsmeldinger = inntektsmeldinger;
    }

    @Override
    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    void setArbeidsgiver(Arbeidsgiver virksomhet) {
        this.arbeidsgiver = virksomhet;
    }

    @Override
    public InntektsmeldingInnsendingsårsak getInntektsmeldingInnsendingsårsak() {
        return innsendingsårsak;
    }

    void setInntektsmeldingInnsendingsårsak(InntektsmeldingInnsendingsårsak innsendingsårsak) {
        this.innsendingsårsak = innsendingsårsak;
    }

    @Override
    public LocalDateTime getInnsendingstidspunkt() {
        return innsendingstidspunkt;
    }

    void setInnsendingstidspunkt(LocalDateTime innsendingstidspunkt) {
        this.innsendingstidspunkt = innsendingstidspunkt;
    }

    @Override
    public String getKanalreferanse() {
        return kanalreferanse;
    }

    void setKanalreferanse(String kanalreferanse) {
        this.kanalreferanse = kanalreferanse;
    }

    @Override
    public String getKildesystem() {
        return kildesystem;
    }

    void setKildesystem(String kildesystem) {
        this.kildesystem = kildesystem;
    }

    @Override
    public List<Gradering> getGraderinger() {
        return Collections.unmodifiableList(graderinger);
    }

    @Override
    public List<NaturalYtelse> getNaturalYtelser() {
        return Collections.unmodifiableList(naturalYtelser);
    }

    @Override
    public List<UtsettelsePeriode> getUtsettelsePerioder() {
        return Collections.unmodifiableList(utsettelsePerioder);
    }

    @Override
    public ArbeidsforholdRef getArbeidsforholdRef() {
        return arbeidsforholdRef != null ? arbeidsforholdRef : ArbeidsforholdRef.ref(null);
    }

    @Override
    public boolean gjelderForEtSpesifiktArbeidsforhold() {
        return getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold();
    }

    /**
     * TODO: (DOKUMENTERE DENNE)
     *
     * @param arbeidsforholdRef Intern arbeidsforhold id
     */
    public void setArbeidsforholdId(ArbeidsforholdRef arbeidsforholdRef) {
        this.arbeidsforholdRef = arbeidsforholdRef;
    }

    @Override
    public LocalDate getStartDatoPermisjon() {
        return startDatoPermisjon;
    }

    void setStartDatoPermisjon(LocalDate startDatoPermisjon) {
        this.startDatoPermisjon = startDatoPermisjon;
    }

    @Override
    public boolean getErNærRelasjon() {
        return nærRelasjon;
    }

    void setNærRelasjon(boolean nærRelasjon) {
        this.nærRelasjon = nærRelasjon;
    }

    @Override
    public Beløp getInntektBeløp() {
        return inntektBeløp;
    }

    void setInntektBeløp(Beløp inntektBeløp) {
        this.inntektBeløp = inntektBeløp;
    }

    @Override
    public Beløp getRefusjonBeløpPerMnd() {
        return refusjonBeløpPerMnd;
    }

    void setRefusjonBeløpPerMnd(Beløp refusjonBeløpPerMnd) {
        this.refusjonBeløpPerMnd = refusjonBeløpPerMnd;
    }

    @Override
    public LocalDate getRefusjonOpphører() {
        return refusjonOpphører;
    }

    void setRefusjonOpphører(LocalDate refusjonOpphører) {
        this.refusjonOpphører = refusjonOpphører;
    }

    @Override
    public List<Refusjon> getEndringerRefusjon() {
        return Collections.unmodifiableList(endringerRefusjon);
    }

    void leggTil(Gradering gradering) {
        this.graderinger.add((GraderingEntitet) gradering);
        ((GraderingEntitet) gradering).setInntektsmelding(this);
    }

    void leggTil(NaturalYtelse naturalYtelse) {
        this.naturalYtelser.add((NaturalYtelseEntitet) naturalYtelse);
        ((NaturalYtelseEntitet) naturalYtelse).setInntektsmelding(this);
    }

    void leggTil(UtsettelsePeriode utsettelsePeriode) {
        this.utsettelsePerioder.add((UtsettelsePeriodeEntitet) utsettelsePeriode);
        ((UtsettelsePeriodeEntitet) utsettelsePeriode).setInntektsmelding(this);
    }

    void leggTil(Refusjon refusjon) {
        this.endringerRefusjon.add((RefusjonEntitet) refusjon);
        ((RefusjonEntitet) refusjon).setInntektsmelding(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InntektsmeldingEntitet entitet = (InntektsmeldingEntitet) o;
        return Objects.equals(getArbeidsgiver(), entitet.getArbeidsgiver()) &&
            Objects.equals(getArbeidsforholdRef(), entitet.getArbeidsforholdRef());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArbeidsgiver(), getArbeidsforholdRef());
    }

    @Override
    public String toString() {
        return "InntektsmeldingEntitet{" +
            "id=" + id +
            ", virksomhet=" + arbeidsgiver +
            ", arbeidsforholdId='" + arbeidsforholdRef + '\'' +
            ", startDatoPermisjon=" + startDatoPermisjon +
            ", nærRelasjon=" + nærRelasjon +
            ", mottattDokumentId=" + mottattDokumentId +
            ", inntektBeløp=" + inntektBeløp +
            ", refusjonBeløpPerMnd=" + refusjonBeløpPerMnd +
            ", refusjonOpphører=" + refusjonOpphører +
            ", innsendingsårsak= " + innsendingsårsak +
            ", innsendingstidspunkt= " + innsendingstidspunkt +
            '}';
    }

}
