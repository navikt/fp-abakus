package no.nav.abakus.iaygrunnlag.v1;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import no.nav.abakus.iaygrunnlag.PersonIdent;
import no.nav.abakus.iaygrunnlag.UuidDto;
import no.nav.abakus.iaygrunnlag.arbeidsforhold.v1.ArbeidsforholdInformasjon;
import no.nav.abakus.iaygrunnlag.inntektsmelding.v1.InntektsmeldingerDto;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.oppgittopptjening.v1.OppgittOpptjeningDto;
import no.nav.abakus.iaygrunnlag.oppgittopptjening.v1.OppgitteOpptjeningerDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class InntektArbeidYtelseGrunnlagDto {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    /**
     * Unk referanse for grunnlaget. Hver versjon av grunnlaget vil få en ny grunnlagReferanse.
     * <p>
     * <h3>Ved skriving</h3> - Hvis referanse ikke finnes fra før vil data skrives ned på nytt og få denne referanse. Hvis de finnes fra før vil
     * det
     * opprettes nytt grunnlag (med ny grunnlagreferanse) der data som sendes ned skrives sammen med data som henger på angitt
     * grunnlagReferanse.
     */
    @JsonProperty(value = "grunnlagReferanse", required = true)
    @Valid
    @NotNull
    private UuidDto grunnlagReferanse;

    /**
     * Brukes til å koble sammen flere grunnlag (der kun siste er aktivt). Normalt behandlingUuid. En koblingReferanse kan spenne over flere
     * grunnlagReferanser. Brukes f.eks. til å finne siste grunnlag på angitt kobling.
     */
    @JsonProperty(value = "koblingReferanse", required = true)
    @Valid
    @NotNull
    private UuidDto koblingReferanse;

    @JsonProperty(value = "grunnlagTidspunkt", required = true)
    @Valid
    @NotNull
    private OffsetDateTime grunnlagTidspunkt = OffsetDateTime.now(DEFAULT_ZONE);

    @JsonProperty(value = "person", required = true)
    @Valid
    @NotNull
    private PersonIdent person;

    @JsonProperty(value = "ytelseType")
    private YtelseType ytelseType = YtelseType.UDEFINERT;

    @JsonProperty(value = "registerGrunnlag")
    @Valid
    private InntektArbeidYtelseAggregatRegisterDto register;

    @JsonProperty(value = "overstyrtGrunnlag")
    @Valid
    private InntektArbeidYtelseAggregatOverstyrtDto overstyrt;

    @JsonProperty(value = "inntektsmeldinger")
    @Valid
    private InntektsmeldingerDto inntektsmeldinger;

    /**
     * Opptjening bruker har oppgitt selv.
     */
    @JsonProperty(value = "oppgittOpptjening")
    @Valid
    private OppgittOpptjeningDto oppgittOpptjening;

    @JsonProperty(value = "overstyrtOppgittOpptjening")
    @Valid
    private OppgittOpptjeningDto overstyrtOppgittOpptjening;

    /**
     * Variant som støtter mer enn en oppgitt opptjening. Den støtter oppgitt opptjening pr. journalpost
     */
    @JsonProperty(value = "oppgitteOpptjeninger")
    @Valid
    private OppgitteOpptjeningerDto oppgitteOpptjeninger;

    @JsonProperty(value = "arbeidsforholdInformasjon")
    @Valid
    private ArbeidsforholdInformasjon arbeidsforholdInformasjon;

    @JsonCreator
    public InntektArbeidYtelseGrunnlagDto(@JsonProperty(value = "person", required = true) @Valid @NotNull PersonIdent person,
                                          @JsonProperty(value = "grunnlagTidspunkt", required = true) @Valid @NotNull OffsetDateTime grunnlagTidspunkt,
                                          @JsonProperty(value = "grunnlagReferanse", required = true) @Valid @NotNull UUID grunnlagReferanse,
                                          @JsonProperty(value = "koblingReferanse", required = true) @Valid @NotNull UUID koblingReferanse,
                                          @JsonProperty(value = "ytelseType") YtelseType ytelseType) {
        Objects.requireNonNull(person, "person");
        Objects.requireNonNull(grunnlagTidspunkt, "grunnlagTidspunkt");
        Objects.requireNonNull(grunnlagReferanse, "grunnlagReferanse");
        Objects.requireNonNull(koblingReferanse, "koblingReferanse");
        this.person = person;
        this.grunnlagReferanse = new UuidDto(grunnlagReferanse);
        this.koblingReferanse = new UuidDto(koblingReferanse);
        this.grunnlagTidspunkt = grunnlagTidspunkt;
        this.ytelseType = ytelseType;
    }

    public InntektArbeidYtelseGrunnlagDto(@JsonProperty(value = "person", required = true) @Valid @NotNull PersonIdent person,
                                          @JsonProperty(value = "grunnlagTidspunkt", required = true) @Valid @NotNull LocalDateTime grunnlagTidspunkt,
                                          @JsonProperty(value = "grunnlagReferanse", required = true) @Valid @NotNull UuidDto grunnlagReferanse,
                                          @JsonProperty(value = "koblingReferanse", required = true) @Valid @NotNull UuidDto koblingReferanse,
                                          @JsonProperty(value = "ytelseType") YtelseType ytelseType) {
        Objects.requireNonNull(person, "person");
        Objects.requireNonNull(grunnlagTidspunkt, "grunnlagTidspunkt");
        Objects.requireNonNull(grunnlagReferanse, "grunnlagReferanse");
        Objects.requireNonNull(koblingReferanse, "koblingReferanse");
        this.koblingReferanse = koblingReferanse;
        this.person = person;
        this.grunnlagReferanse = grunnlagReferanse;
        this.grunnlagTidspunkt = grunnlagTidspunkt.atZone(DEFAULT_ZONE).toOffsetDateTime();
        this.ytelseType = ytelseType;
    }

    protected InntektArbeidYtelseGrunnlagDto() {
        // default ctor
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !(obj.getClass().equals(this.getClass()))) {
            return false;
        }
        InntektArbeidYtelseGrunnlagDto other = (InntektArbeidYtelseGrunnlagDto) obj;
        //TODO bør sammenligne alle feltene, eller ha en kommentar som sier hvorfor kun utvalgte felt brukes i equals-metoden
        return Objects.equals(person, other.person) && Objects.equals(register, other.register) && Objects.equals(overstyrt, other.overstyrt);
    }

    public ArbeidsforholdInformasjon getArbeidsforholdInformasjon() {
        return arbeidsforholdInformasjon;
    }

    public void setArbeidsforholdInformasjon(ArbeidsforholdInformasjon arbeidsforholdInformasjon) {
        this.arbeidsforholdInformasjon = arbeidsforholdInformasjon;
    }

    public String getGrunnlagReferanse() {
        return grunnlagReferanse == null ? null : grunnlagReferanse.getReferanse();
    }

    public OffsetDateTime getGrunnlagTidspunkt() {
        return grunnlagTidspunkt;
    }

    public InntektsmeldingerDto getInntektsmeldinger() {
        return inntektsmeldinger;
    }

    public String getKoblingReferanse() {
        return koblingReferanse == null ? null : koblingReferanse.getReferanse();
    }

    public OppgittOpptjeningDto getOppgittOpptjening() {
        return oppgittOpptjening;
    }

    public void setOppgittOpptjening(OppgittOpptjeningDto oppgittOpptjening) {
        if (oppgitteOpptjeninger != null) {
            throw new IllegalArgumentException(
                "Skal ikke bruke både ny (oppgitt opptjening pr journalpostId) og gammel (en oppgitt opptjening) i samme sak.");
        }
        this.oppgittOpptjening = oppgittOpptjening;
    }

    public OppgittOpptjeningDto getOverstyrtOppgittOpptjening() {
        return overstyrtOppgittOpptjening;
    }

    public void setOverstyrtOppgittOpptjening(OppgittOpptjeningDto overstyrtOppgittOpptjening) {
        if (oppgitteOpptjeninger != null) {
            throw new IllegalArgumentException(
                "Skal ikke bruke både ny (oppgitt opptjening pr journalpostId) og gammel (en oppgitt opptjening) i samme sak.");
        }
        this.overstyrtOppgittOpptjening = overstyrtOppgittOpptjening;
    }

    public OppgitteOpptjeningerDto getOppgitteOpptjeninger() {
        return oppgitteOpptjeninger;
    }

    public InntektArbeidYtelseAggregatOverstyrtDto getOverstyrt() {
        return overstyrt;
    }

    public void setOverstyrt(InntektArbeidYtelseAggregatOverstyrtDto overstyrt) {
        this.overstyrt = overstyrt;
    }

    public PersonIdent getPerson() {
        return person;
    }

    public YtelseType getYtelseType() {
        return this.ytelseType;
    }

    public InntektArbeidYtelseAggregatRegisterDto getRegister() {
        return register;
    }

    public void setRegister(InntektArbeidYtelseAggregatRegisterDto register) {
        this.register = register;
    }

    @Override
    public int hashCode() {
        return Objects.hash(person, register, overstyrt);
    }

    public InntektArbeidYtelseGrunnlagDto medArbeidsforholdInformasjon(ArbeidsforholdInformasjon arbeidsforholdInformasjon) {
        setArbeidsforholdInformasjon(arbeidsforholdInformasjon);
        return this;
    }

    public InntektArbeidYtelseGrunnlagDto medInntektsmeldinger(InntektsmeldingerDto inntektsmeldinger) {
        this.inntektsmeldinger = inntektsmeldinger;
        return this;
    }

    public InntektArbeidYtelseGrunnlagDto medOppgittOpptjening(OppgittOpptjeningDto oppgittOpptjening) {
        setOppgittOpptjening(oppgittOpptjening);
        return this;
    }

    public InntektArbeidYtelseGrunnlagDto medOppgittOpptjeninger(OppgitteOpptjeningerDto oppgitteOpptjeninger) {
        setOppgittOpptjeningPrDokument(oppgitteOpptjeninger);
        return this;
    }

    public InntektArbeidYtelseGrunnlagDto medOverstyrtOppgittOpptjening(OppgittOpptjeningDto overstyrtOppgittOpptjening) {
        setOverstyrtOppgittOpptjening(overstyrtOppgittOpptjening);
        return this;
    }

    public InntektArbeidYtelseGrunnlagDto medOverstyrt(InntektArbeidYtelseAggregatOverstyrtDto overstyrt) {
        setOverstyrt(overstyrt);
        return this;
    }

    public InntektArbeidYtelseGrunnlagDto medRegister(InntektArbeidYtelseAggregatRegisterDto register) {
        setRegister(register);
        return this;
    }

    public void setOppgittOpptjeningPrDokument(OppgitteOpptjeningerDto oppgitteOpptjeninger) {
        if (oppgittOpptjening != null || overstyrtOppgittOpptjening != null) {
            throw new IllegalArgumentException(
                "Skal ikke bruke både ny (oppgitt opptjening pr journalpostId) og gammel (en oppgitt opptjening) i samme sak.");
        }
        this.oppgitteOpptjeninger = oppgitteOpptjeninger;
    }

}
