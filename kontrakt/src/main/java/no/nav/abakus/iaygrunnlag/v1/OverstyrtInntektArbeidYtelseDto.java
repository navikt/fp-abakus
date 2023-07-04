package no.nav.abakus.iaygrunnlag.v1;

import java.util.Objects;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.iaygrunnlag.PersonIdent;
import no.nav.abakus.iaygrunnlag.arbeidsforhold.v1.ArbeidsforholdInformasjon;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class OverstyrtInntektArbeidYtelseDto {

    @JsonProperty(value = "ytelseType")
    @NotNull
    private YtelseType ytelseType;

    /**
     * Angi hvem grunnlaget hentes for.
     */
    @JsonProperty(value = "personIdent", required = true)
    @Valid
    @NotNull
    private PersonIdent person;

    /**
     * Brukes til å koble sammen flere grunnlag (der kun siste er aktivt). Normalt behandlingUuid. En koblingReferanse kan spenne over flere
     * grunnlagReferanser. Brukes f.eks. til å finne siste grunnlag på angitt kobling.
     */
    @JsonProperty(value = "koblingReferanse")
    @Valid
    private UUID koblingReferanse;

    /**
     * Unk referanse for grunnlaget. Hver versjon av grunnlaget vil få en ny grunnlagReferanse.
     * <p>
     * <h3>Ved skriving</h3> - Hvis referanse ikke finnes fra før vil data skrives ned på nytt og få denne referanse. Hvis de finnes fra før vil
     * det
     * opprettes nytt grunnlag (med ny grunnlagreferanse) der data som sendes ned skrives sammen med data som henger på angitt
     * grunnlagReferanse.
     */
    @JsonProperty(value = "grunnlagReferanse")
    @Valid
    private UUID grunnlagReferanse;

    @JsonProperty(value = "overstyrt")
    @Valid
    private InntektArbeidYtelseAggregatOverstyrtDto overstyrt;

    /**
     * Referanser til arbeidsforhold satt av saksbehandler.
     */
    @JsonProperty(value = "arbeidsforholdInformasjon")
    @Valid
    private ArbeidsforholdInformasjon arbeidsforholdInformasjon;

    @JsonCreator
    public OverstyrtInntektArbeidYtelseDto(@JsonProperty(value = "personIdent", required = true) PersonIdent person,
                                           @JsonProperty(value = "grunnlagReferanse") @Valid UUID grunnlagReferanse,
                                           @JsonProperty(value = "koblingReferanse") @Valid UUID koblingReferanse,
                                           @JsonProperty(value = "ytelseType") YtelseType ytelseType,
                                           @JsonProperty(value = "arbeidsforholdInformasjon") ArbeidsforholdInformasjon arbeidsforholdInformasjon,
                                           @JsonProperty(value = "overstyrt") InntektArbeidYtelseAggregatOverstyrtDto overstyrt) {
        this.ytelseType = Objects.requireNonNull(ytelseType, "ytelseType");
        this.person = Objects.requireNonNull(person, "person");
        if (koblingReferanse == null && grunnlagReferanse == null) {
            throw new IllegalArgumentException("Må oppgi minst en av koblingReferanse, grunnlagReferanse");
        }
        this.grunnlagReferanse = grunnlagReferanse;
        this.koblingReferanse = koblingReferanse;

        this.arbeidsforholdInformasjon = arbeidsforholdInformasjon;
        this.overstyrt = overstyrt;
    }

    public ArbeidsforholdInformasjon getArbeidsforholdInformasjon() {
        return arbeidsforholdInformasjon;
    }

    public UUID getGrunnlagReferanse() {
        return grunnlagReferanse;
    }

    public UUID getKoblingReferanse() {
        return koblingReferanse;
    }

    public PersonIdent getPerson() {
        return this.person;
    }

    public InntektArbeidYtelseAggregatOverstyrtDto getOverstyrt() {
        return overstyrt;
    }

    public YtelseType getYtelseType() {
        return this.ytelseType;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + "person=*****" + ", ytelseType=" + ytelseType + (
            koblingReferanse == null ? "" : ", koblingReferanse=" + koblingReferanse) + (
            grunnlagReferanse == null ? "" : ", grunnlagReferanse" + grunnlagReferanse) + (overstyrt == null ? "" : ", overstyrtIay={...}") + (
            arbeidsforholdInformasjon == null ? "" : ", arbeidsforholdInformasjon={...}") + ">";
    }

}
