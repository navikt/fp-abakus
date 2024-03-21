package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.annotations.NaturalId;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjening;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.DiffIgnore;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "InntektArbeidGrunnlag")
@Table(name = "GR_ARBEID_INNTEKT")
public class InntektArbeidYtelseGrunnlag extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GR_ARBEID_INNTEKT")
    private Long id;

    @DiffIgnore
    @Column(name = "kobling_id", nullable = false, updatable = false, unique = true)
    private Long koblingId;

    @NaturalId
    @DiffIgnore
    @Embedded
    @AttributeOverrides({@AttributeOverride(name = "referanse", column = @Column(name = "grunnlag_referanse", updatable = false, unique = true))})
    private GrunnlagReferanse grunnlagReferanse;

    @OneToOne
    @JoinColumn(name = "register_id", updatable = false, unique = true)
    @ChangeTracked
    private InntektArbeidYtelseAggregat register;

    @OneToOne
    @JoinColumn(name = "saksbehandlet_id", updatable = false, unique = true)
    @ChangeTracked
    private InntektArbeidYtelseAggregat saksbehandlet;

    /**
     * versjon 1 - støtter kun en oppgitt opptjening på en behandling, kan heller ikke oppdateres
     */
    @OneToOne
    @JoinColumn(name = "oppgitt_opptjening_id", updatable = false, unique = true)
    @ChangeTracked
    private OppgittOpptjening oppgittOpptjening;

    @OneToOne
    @ChangeTracked
    @JoinColumn(name = "inntektsmeldinger_id", updatable = false, unique = true)
    private InntektsmeldingAggregat inntektsmeldinger;

    @ChangeTracked
    @OneToOne
    @JoinColumn(name = "informasjon_id", updatable = false, unique = true)
    private ArbeidsforholdInformasjon arbeidsforholdInformasjon;

    // Kun for Frisinn
    @OneToOne
    @JoinColumn(name = "overstyrt_oppgitt_opptjening_id", updatable = false, unique = true)
    @ChangeTracked
    private OppgittOpptjening overstyrtOppgittOpptjening;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @SuppressWarnings("unused")
    private InntektArbeidYtelseGrunnlag() {
        // Plattform (CDI, Hibernate, Jackson)
    }

    InntektArbeidYtelseGrunnlag(InntektArbeidYtelseGrunnlag grunnlag) {
        this(UUID.randomUUID(), grunnlag.getOpprettetTidspunkt());

        // NB! skal ikke lage ny versjon av oppgitt opptjening! Lenker bare inn
        grunnlag.getOppgittOpptjening().ifPresent(kopiAvOppgittOpptjening -> this.setOppgittOpptjening(kopiAvOppgittOpptjening));

        grunnlag.getOverstyrtOppgittOpptjening().ifPresent(this::setOverstyrtOppgittOpptjening);
        grunnlag.getRegisterVersjon().ifPresent(nyRegisterVerson -> this.setRegister(nyRegisterVerson));

        grunnlag.getSaksbehandletVersjon().ifPresent(nySaksbehandletFørVersjon -> this.setSaksbehandlet(nySaksbehandletFørVersjon));

        grunnlag.getInntektsmeldinger().ifPresent(this::setInntektsmeldinger);

        grunnlag.getArbeidsforholdInformasjon().ifPresent(info -> this.setInformasjon(info));
    }

    InntektArbeidYtelseGrunnlag(GrunnlagReferanse grunnlagReferanse, LocalDateTime opprettetTidspunkt) {
        this.grunnlagReferanse = Objects.requireNonNull(grunnlagReferanse, "grunnlagReferanse");
        setOpprettetTidspunkt(opprettetTidspunkt);
    }

    InntektArbeidYtelseGrunnlag(UUID grunnlagReferanse, LocalDateTime opprettetTidspunkt) {
        this(new GrunnlagReferanse(Objects.requireNonNull(grunnlagReferanse, "grunnlagReferanse")), opprettetTidspunkt);
    }

    public Long getKoblingId() {
        return koblingId;
    }

    public GrunnlagReferanse getGrunnlagReferanse() {
        return grunnlagReferanse;
    }

    void setGrunnlagReferanse(GrunnlagReferanse grunnlagReferanse) {
        if (this.koblingId != null && !Objects.equals(this.grunnlagReferanse, grunnlagReferanse)) {
            throw new IllegalStateException(String.format("Kan ikke overskrive grunnlagReferanse %s: %s", this.grunnlagReferanse, grunnlagReferanse));
        }
        this.grunnlagReferanse = grunnlagReferanse;
    }

    /**
     * Returnerer en overstyrt versjon av aggregat. Hvis saksbehandler har løst et aksjonspunkt i forbindele med
     * opptjening vil det finnes et overstyrt aggregat, gjelder for FØR første dag i permisjonsuttaket (skjæringstidspunktet)
     */
    public Optional<InntektArbeidYtelseAggregat> getSaksbehandletVersjon() {
        return Optional.ofNullable(saksbehandlet);
    }

    void setSaksbehandlet(InntektArbeidYtelseAggregat saksbehandletFør) {
        this.saksbehandlet = saksbehandletFør;
    }

    /**
     * Returnerer innhentede registeropplysninger som aggregat. Tar ikke hensyn til saksbehandlers overstyringer (se
     * {@link #getSaksbehandletVersjon()}.
     */
    public Optional<InntektArbeidYtelseAggregat> getRegisterVersjon() {
        return Optional.ofNullable(register);
    }

    /**
     * Returnerer aggregat som holder alle inntektsmeldingene som benyttes i behandlingen.
     */
    public Optional<InntektsmeldingAggregat> getInntektsmeldinger() {
        return Optional.ofNullable(inntektsmeldinger);
    }

    void setInntektsmeldinger(InntektsmeldingAggregat inntektsmeldingAggregat) {
        this.inntektsmeldinger = inntektsmeldingAggregat;
    }

    /**
     * Returnerer oppgitt opptjening hvis det finnes. (Inneholder opplysninger søker opplyser om i søknaden)
     */
    public Optional<OppgittOpptjening> getOppgittOpptjening() {
        return Optional.ofNullable(oppgittOpptjening);
    }

    void setOppgittOpptjening(OppgittOpptjening oppgittOpptjening) {
        this.oppgittOpptjening = oppgittOpptjening;
    }

    /**
     * Returnerer overstyrt oppgitt opptjening hvis det finnes. (Inneholder opplysninger søker opplyser om i søknaden)
     */
    public Optional<OppgittOpptjening> getOverstyrtOppgittOpptjening() {
        return Optional.ofNullable(overstyrtOppgittOpptjening);
    }

    /**
     * Returnerer overstyrt oppgitt opptjening hvis det finnes, eller vanlig oppgitt opptjening
     */
    public Optional<OppgittOpptjening> getGjeldendeOppgittOpptjening() {
        return getOverstyrtOppgittOpptjening().or(this::getOppgittOpptjening);
    }

    void setOverstyrtOppgittOpptjening(OppgittOpptjening overstyrtOppgittOpptjening) {
        this.overstyrtOppgittOpptjening = overstyrtOppgittOpptjening;
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

    /**
     * Hvorvidt dette er det siste (aktive grunnlaget) for en behandling.
     */
    public boolean isAktiv() {
        return aktiv;
    }

    /**
     * Unik id for dette grunnlaget.
     */
    public Long getId() {
        return id;
    }

    void setRegister(InntektArbeidYtelseAggregat registerFør) {
        this.register = registerFør;
    }

    public Optional<ArbeidsforholdInformasjon> getArbeidsforholdInformasjon() {
        return Optional.ofNullable(arbeidsforholdInformasjon);
    }

    void setInformasjon(ArbeidsforholdInformasjon informasjon) {
        this.arbeidsforholdInformasjon = informasjon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof InntektArbeidYtelseGrunnlag)) {
            return false;
        }
        var that = (InntektArbeidYtelseGrunnlag) o;
        return aktiv == that.aktiv && Objects.equals(register, that.register) && Objects.equals(saksbehandlet, that.saksbehandlet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(register, saksbehandlet);
    }

    void fjernSaksbehandlet() {
        saksbehandlet = null;
    }

}
