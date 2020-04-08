package no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding;

import java.math.BigDecimal;
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
import javax.persistence.Version;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.InntektsmeldingAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsmeldingInnsendingsårsak;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.JournalpostId;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "Inntektsmelding")
@Table(name = "IAY_INNTEKTSMELDING")
public class Inntektsmelding extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INNTEKTSMELDING")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inntektsmeldinger_id", nullable = false, updatable = false)
    private InntektsmeldingAggregat inntektsmeldinger;

    @OneToMany(mappedBy = "inntektsmelding")
    @ChangeTracked
    private List<Gradering> graderinger = new ArrayList<>();

    @OneToMany(mappedBy = "inntektsmelding")
    @ChangeTracked
    private List<NaturalYtelse> naturalYtelser = new ArrayList<>();

    @OneToMany(mappedBy = "inntektsmelding")
    @ChangeTracked
    private List<Fravær> oppgittFravær = new ArrayList<>();

    @OneToMany(mappedBy = "inntektsmelding")
    @ChangeTracked
    private List<UtsettelsePeriode> utsettelsePerioder = new ArrayList<>();

    @Embedded
    @ChangeTracked
    private Arbeidsgiver arbeidsgiver;

    @Embedded
    @ChangeTracked
    private InternArbeidsforholdRef arbeidsforholdRef;

    @Column(name = "start_dato_permisjon", updatable = false, nullable = false)
    @ChangeTracked
    private LocalDate startDatoPermisjon;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "naer_relasjon", updatable = false, nullable = false)
    private boolean nærRelasjon;

    @Embedded
    private JournalpostId journalpostId;

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

    @Column(name = "mottatt_dato", nullable = false, updatable = false)
    private LocalDate mottattDato;

    @OneToMany(mappedBy = "inntektsmelding")
    @ChangeTracked
    private List<Refusjon> endringerRefusjon = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "innsendingsaarsak", referencedColumnName = "kode", nullable = false)),
            @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + InntektsmeldingInnsendingsårsak.DISCRIMINATOR
                + "'")) })
    @ChangeTracked
    private InntektsmeldingInnsendingsårsak innsendingsårsak = InntektsmeldingInnsendingsårsak.UDEFINERT;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    Inntektsmelding() {
    }

    /** copy ctor. */
    public Inntektsmelding(Inntektsmelding inntektsmelding) {
        this.arbeidsgiver = inntektsmelding.getArbeidsgiver();
        this.arbeidsforholdRef = inntektsmelding.getArbeidsforholdRef();
        this.startDatoPermisjon = inntektsmelding.getStartDatoPermisjon();
        this.nærRelasjon = inntektsmelding.getErNærRelasjon();
        this.journalpostId = inntektsmelding.getJournalpostId();
        this.inntektBeløp = inntektsmelding.getInntektBeløp();
        this.refusjonBeløpPerMnd = inntektsmelding.getRefusjonBeløpPerMnd();
        this.refusjonOpphører = inntektsmelding.getRefusjonOpphører();
        this.innsendingsårsak = inntektsmelding.getInntektsmeldingInnsendingsårsak();
        this.innsendingstidspunkt = inntektsmelding.getInnsendingstidspunkt();
        this.kanalreferanse = inntektsmelding.getKanalreferanse();
        this.kildesystem = inntektsmelding.getKildesystem();
        this.mottattDato = inntektsmelding.getMottattDato();

        this.graderinger = inntektsmelding.getGraderinger().stream().map(g -> {
            var data = new Gradering(g);
            data.setInntektsmelding(this);
            return data;
        }).collect(Collectors.toList());
        this.naturalYtelser = inntektsmelding.getNaturalYtelser().stream().map(n -> {
            var data = new NaturalYtelse(n);
            data.setInntektsmelding(this);
            return data;
        }).collect(Collectors.toList());
        this.utsettelsePerioder = inntektsmelding.getUtsettelsePerioder().stream().map(u -> {
            var data = new UtsettelsePeriode(u);
            data.setInntektsmelding(this);
            return data;
        }).collect(Collectors.toList());
        this.endringerRefusjon = inntektsmelding.getEndringerRefusjon().stream().map(r -> {
            var data = new Refusjon(r);
            data.setInntektsmelding(this);
            return data;
        }).collect(Collectors.toList());
        this.oppgittFravær = inntektsmelding.getOppgittFravær().stream().map(f -> {
            var data = new Fravær(f);
            data.setInntektsmelding(this);
            return data;
        }).collect(Collectors.toList());
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = { arbeidsgiver, arbeidsforholdRef };
        return IndexKeyComposer.createKey(keyParts);
    }

    public void setInntektsmeldinger(InntektsmeldingAggregat inntektsmeldinger) {
        this.inntektsmeldinger = inntektsmeldinger;
    }

    /**
     * Arbeidsgiveren som har sendt inn inntektsmeldingen
     *
     * @return {@link ArbeidsgiverEntitet}
     */
    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    void setArbeidsgiver(Arbeidsgiver virksomhet) {
        this.arbeidsgiver = virksomhet;
    }

    public InntektsmeldingInnsendingsårsak getInntektsmeldingInnsendingsårsak() {
        return innsendingsårsak;
    }

    void setInntektsmeldingInnsendingsårsak(InntektsmeldingInnsendingsårsak innsendingsårsak) {
        this.innsendingsårsak = innsendingsårsak;
    }

    public LocalDateTime getInnsendingstidspunkt() {
        return innsendingstidspunkt;
    }

    void setInnsendingstidspunkt(LocalDateTime innsendingstidspunkt) {
        this.innsendingstidspunkt = innsendingstidspunkt;
    }

    public String getKanalreferanse() {
        return kanalreferanse;
    }

    /** Dato inntektsmelding mottatt i NAV (tilsvarer dato lagret i Joark). */
    public LocalDate getMottattDato() {
        return mottattDato;
    }

    void setKanalreferanse(String kanalreferanse) {
        this.kanalreferanse = kanalreferanse;
    }

    void setMottattDato(LocalDate mottattDato) {
        this.mottattDato = mottattDato;
    }

    public String getKildesystem() {
        return kildesystem;
    }

    void setKildesystem(String kildesystem) {
        this.kildesystem = kildesystem;
    }

    public List<Fravær> getOppgittFravær() {
        return Collections.unmodifiableList(oppgittFravær);
    }

    /**
     * Liste over perioder med graderinger
     *
     * @return {@link Gradering}
     */
    public List<Gradering> getGraderinger() {
        return Collections.unmodifiableList(graderinger);
    }

    /**
     * Liste over naturalytelser
     *
     * @return {@link NaturalYtelse}
     */
    public List<NaturalYtelse> getNaturalYtelser() {
        return Collections.unmodifiableList(naturalYtelser);
    }

    /**
     * Liste over utsettelse perioder
     *
     * @return {@link UtsettelsePeriode}
     */
    public List<UtsettelsePeriode> getUtsettelsePerioder() {
        return Collections.unmodifiableList(utsettelsePerioder);
    }

    /**
     * Arbeidsgivers arbeidsforhold referanse
     *
     * @return {@link ArbeidsforholdRef}
     */
    public InternArbeidsforholdRef getArbeidsforholdRef() {
        return arbeidsforholdRef != null ? arbeidsforholdRef : InternArbeidsforholdRef.nullRef();
    }

    /**
     * Gjelder for et spesifikt arbeidsforhold
     *
     * @return {@link Boolean}
     */
    public boolean gjelderForEtSpesifiktArbeidsforhold() {
        return getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold();
    }

    public boolean gjelderSammeArbeidsforhold(Inntektsmelding annen) {
        return getArbeidsgiver().equals(annen.getArbeidsgiver())
            && getArbeidsforholdRef().gjelderFor(annen.getArbeidsforholdRef());
    }

    /**
     * Setter intern arbeidsdforhold Id for inntektsmelding
     *
     * @param arbeidsforholdRef Intern arbeidsforhold id
     */
    void setArbeidsforholdId(InternArbeidsforholdRef arbeidsforholdRef) {
        this.arbeidsforholdRef = arbeidsforholdRef != null ? arbeidsforholdRef : InternArbeidsforholdRef.nullRef();
    }

    /**
     * Startdato for permisjonen
     *
     * @return {@link LocalDate}
     */
    public LocalDate getStartDatoPermisjon() {
        return startDatoPermisjon;
    }

    /**
     * Referanse til {@link no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument} som benyttes for å markere
     * hvilke dokument som er gjeldende i behandlingen
     *
     * @return {@link Long}
     */
    public JournalpostId getJournalpostId() {
        return journalpostId;
    }

    void setJournalpostId(JournalpostId journalpostId) {
        this.journalpostId = journalpostId;
    }

    void setStartDatoPermisjon(LocalDate startDatoPermisjon) {
        this.startDatoPermisjon = startDatoPermisjon;
    }

    /**
     * Er det nær relasjon mellom søker og arbeidsgiver
     *
     * @return {@link Boolean}
     */
    public boolean getErNærRelasjon() {
        return nærRelasjon;
    }

    void setNærRelasjon(boolean nærRelasjon) {
        this.nærRelasjon = nærRelasjon;
    }

    /**
     * Oppgitt årsinntekt fra arbeidsgiver
     *
     * @return {@link BigDecimal}
     */
    public Beløp getInntektBeløp() {
        return inntektBeløp;
    }

    void setInntektBeløp(Beløp inntektBeløp) {
        this.inntektBeløp = inntektBeløp;
    }

    /**
     * Beløpet arbeidsgiver ønsker refundert
     *
     * @return {@link BigDecimal}
     */
    public Beløp getRefusjonBeløpPerMnd() {
        return refusjonBeløpPerMnd;
    }

    void setRefusjonBeløpPerMnd(Beløp refusjonBeløpPerMnd) {
        this.refusjonBeløpPerMnd = refusjonBeløpPerMnd;
    }

    /**
     * Dersom refusjonen opphører i stønadsperioden angis siste dag det søkes om refusjon for.
     *
     * @return {@link LocalDate}
     */
    public LocalDate getRefusjonOpphører() {
        return refusjonOpphører;
    }

    void setRefusjonOpphører(LocalDate refusjonOpphører) {
        this.refusjonOpphører = refusjonOpphører;
    }

    /**
     * Liste over endringer i refusjonsbeløp
     *
     * @return {@Link Refusjon}
     */

    public List<Refusjon> getEndringerRefusjon() {
        return Collections.unmodifiableList(endringerRefusjon);
    }

    void leggTil(Gradering gradering) {
        this.graderinger.add(gradering);
        gradering.setInntektsmelding(this);
    }

    void leggTil(NaturalYtelse naturalYtelse) {
        this.naturalYtelser.add(naturalYtelse);
        naturalYtelse.setInntektsmelding(this);
    }

    void leggTil(UtsettelsePeriode utsettelsePeriode) {
        this.utsettelsePerioder.add(utsettelsePeriode);
        utsettelsePeriode.setInntektsmelding(this);
    }

    void leggTil(Refusjon refusjon) {
        this.endringerRefusjon.add(refusjon);
        refusjon.setInntektsmelding(this);
    }

    void leggTil(Fravær fravær) {
        this.oppgittFravær.add(fravær);
        fravær.setInntektsmelding(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof Inntektsmelding)) {
            return false;
        }
        var entitet = (Inntektsmelding) o;
        return Objects.equals(getArbeidsgiver(), entitet.getArbeidsgiver()) &&
            Objects.equals(getJournalpostId(), entitet.getJournalpostId()) &&
            Objects.equals(getArbeidsforholdRef(), entitet.getArbeidsforholdRef());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArbeidsgiver(), getArbeidsforholdRef(), getJournalpostId());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" +
            "id=" + id +
            ", virksomhet=" + arbeidsgiver +
            ", arbeidsforholdId='" + arbeidsforholdRef + '\'' +
            ", startDatoPermisjon=" + startDatoPermisjon +
            ", nærRelasjon=" + nærRelasjon +
            ", journalpostId=" + journalpostId +
            ", inntektBeløp=" + inntektBeløp +
            ", refusjonBeløpPerMnd=" + refusjonBeløpPerMnd +
            ", refusjonOpphører=" + refusjonOpphører +
            ", innsendingsårsak= " + innsendingsårsak +
            ", innsendingstidspunkt= " + innsendingstidspunkt +
            '>';
    }

}
