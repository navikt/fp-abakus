package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.NaturalId;

import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.DiffIgnore;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "InntektArbeidGrunnlag")
@Table(name = "GR_ARBEID_INNTEKT")
public class InntektArbeidYtelseGrunnlagEntitet extends BaseEntitet implements InntektArbeidYtelseGrunnlag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GR_ARBEID_INNTEKT")
    private Long id;

    @DiffIgnore
    @Column(name = "kobling_id", nullable = false, updatable = false, unique = true)
    private Long koblingId;

    @NaturalId
    @DiffIgnore
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "referanse", column = @Column(name = "grunnlag_referanse", updatable = false, unique = true))
    })
    private GrunnlagReferanse grunnlagReferanse;

    @OneToOne
    @JoinColumn(name = "register_id", updatable = false, unique = true)
    @ChangeTracked
    private InntektArbeidYtelseAggregatEntitet register;

    @OneToOne
    @JoinColumn(name = "saksbehandlet_id", updatable = false, unique = true)
    @ChangeTracked
    private InntektArbeidYtelseAggregatEntitet saksbehandlet;

    @OneToOne
    @JoinColumn(name = "oppgitt_opptjening_id", updatable = false, unique = true)
    @ChangeTracked
    private OppgittOpptjeningEntitet oppgittOpptjening;

    @OneToOne
    @ChangeTracked
    @JoinColumn(name = "inntektsmeldinger_id", updatable = false, unique = true)
    private InntektsmeldingAggregatEntitet inntektsmeldinger;

    @ChangeTracked
    @OneToOne
    @JoinColumn(name = "informasjon_id", updatable = false, unique = true)
    private ArbeidsforholdInformasjonEntitet arbeidsforholdInformasjon;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @SuppressWarnings("unused")
    private InntektArbeidYtelseGrunnlagEntitet() {
    }

    InntektArbeidYtelseGrunnlagEntitet(InntektArbeidYtelseGrunnlag grunnlag) {
        this(UUID.randomUUID(), grunnlag.getOpprettetTidspunkt());

        // NB! skal ikke lage ny versjon av oppgitt opptjening! Lenker bare inn
        grunnlag.getOppgittOpptjening().ifPresent(kopiAvOppgittOpptjening -> this.setOppgittOpptjening((OppgittOpptjeningEntitet) kopiAvOppgittOpptjening));
        grunnlag.getRegisterVersjon().ifPresent(nyRegisterVerson -> this.setRegister((InntektArbeidYtelseAggregatEntitet) nyRegisterVerson));

        grunnlag.getSaksbehandletVersjon()
            .ifPresent(nySaksbehandletFørVersjon -> this.setSaksbehandlet((InntektArbeidYtelseAggregatEntitet) nySaksbehandletFørVersjon));

        grunnlag.getInntektsmeldinger().ifPresent(this::setInntektsmeldinger);

        grunnlag.getArbeidsforholdInformasjon().ifPresent(info -> this.setInformasjon((ArbeidsforholdInformasjonEntitet) info));
    }

    InntektArbeidYtelseGrunnlagEntitet(GrunnlagReferanse grunnlagReferanse, LocalDateTime opprettetTidspunkt) {
        this.grunnlagReferanse = Objects.requireNonNull(grunnlagReferanse, "grunnlagReferanse");
        setOpprettetTidspunkt(opprettetTidspunkt);
    }

    InntektArbeidYtelseGrunnlagEntitet(UUID grunnlagReferanse, LocalDateTime opprettetTidspunkt) {
        this(new GrunnlagReferanse(Objects.requireNonNull(grunnlagReferanse, "grunnlagReferanse")), opprettetTidspunkt);
    }

    @Override
    public Long getKoblingId() {
        return koblingId;
    }

    @Override
    public GrunnlagReferanse getGrunnlagReferanse() {
        return grunnlagReferanse;
    }

    void setGrunnlagReferanse(GrunnlagReferanse grunnlagReferanse) {
        if (this.koblingId != null && !Objects.equals(this.grunnlagReferanse, grunnlagReferanse)) {
            throw new IllegalStateException(String.format("Kan ikke overskrive grunnlagReferanse %s: %s", this.grunnlagReferanse, grunnlagReferanse));
        }
        this.grunnlagReferanse = grunnlagReferanse;
    }

    @Override
    public Optional<InntektArbeidYtelseAggregat> getSaksbehandletVersjon() {
        return Optional.ofNullable(saksbehandlet);
    }

    void setSaksbehandlet(InntektArbeidYtelseAggregatEntitet saksbehandletFør) {
        this.saksbehandlet = saksbehandletFør;
    }

    @Override
    public Optional<InntektArbeidYtelseAggregat> getRegisterVersjon() {
        return Optional.ofNullable(register);
    }

    @Override
    public Optional<InntektsmeldingAggregat> getInntektsmeldinger() {
        return Optional.ofNullable(inntektsmeldinger);
    }

    void setInntektsmeldinger(InntektsmeldingAggregat inntektsmeldingAggregat) {
        this.inntektsmeldinger = (InntektsmeldingAggregatEntitet) inntektsmeldingAggregat;
    }

    @Override
    public Optional<OppgittOpptjening> getOppgittOpptjening() {
        return Optional.ofNullable(oppgittOpptjening);
    }

    void setOppgittOpptjening(OppgittOpptjeningEntitet oppgittOpptjening) {
        this.oppgittOpptjening = oppgittOpptjening;
    }

    void setKobling(Long koblingId) {
        if (this.koblingId != null && !Objects.equals(this.koblingId, koblingId)) {
            throw new IllegalStateException(String.format("Kan ikke overskrive koblingId %s: %s", this.koblingId, koblingId));
        }
        this.koblingId = koblingId;
    }

    void setAktivt(boolean aktiv) {
        this.aktiv = aktiv;
    }

    @Override
    public boolean isAktiv() {
        return aktiv;
    }

    @Override
    public Long getId() {
        return id;
    }

    void setRegister(InntektArbeidYtelseAggregatEntitet registerFør) {
        this.register = registerFør;
    }

    @Override
    public Optional<ArbeidsforholdInformasjon> getArbeidsforholdInformasjon() {
        return Optional.ofNullable(arbeidsforholdInformasjon);
    }

    void setInformasjon(ArbeidsforholdInformasjonEntitet informasjon) {
        this.arbeidsforholdInformasjon = informasjon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof InntektArbeidYtelseGrunnlagEntitet))
            return false;
        var that = (InntektArbeidYtelseGrunnlagEntitet) o;
        return aktiv == that.aktiv &&
            Objects.equals(register, that.register) &&
            Objects.equals(saksbehandlet, that.saksbehandlet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(register, saksbehandlet);
    }

    void fjernSaksbehandlet() {
        saksbehandlet = null;
    }
}
