package no.nav.abakus.iaygrunnlag.request;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.abakus.iaygrunnlag.PersonIdent;
import no.nav.abakus.iaygrunnlag.arbeidsforhold.v1.ArbeidsforholdInformasjon;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.v1.InntektArbeidYtelseAggregatOverstyrtDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
public class OverstyrGrunnlagRequest {

    @JsonProperty(value = "saksnummer", required = true)
    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9_\\.\\-:]+$", message = "[${validatedValue}] matcher ikke tillatt pattern '{value}'")
    @Valid
    private String saksnummer;

    @JsonProperty(value = "ytelseType")
    @NotNull
    @Valid
    private YtelseType ytelseType = YtelseType.UDEFINERT;

    @JsonProperty(value = "aktør", required = true)
    @NotNull
    @Valid
    private PersonIdent aktør;

    /**
     * Gir siste grunnlag på koblingen dersom grunnlagReferanse ikke er satt.
     */
    @JsonProperty(value = "koblingReferanse", required = true)
    @Valid
    private UUID koblingReferanse;

    /**
     * Unk referanse for grunnlaget som skal overstyres.
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
    public OverstyrGrunnlagRequest(@JsonProperty(value = "aktør", required = true) @NotNull @Valid PersonIdent aktør,
                                   @JsonProperty(value = "saksnummer", required = true) @NotNull @Valid String saksnummer,
                                   @JsonProperty(value = "ytelseType") @NotNull @Valid YtelseType ytelseType,
                                   @JsonProperty(value = "grunnlagReferanse") @Valid UUID grunnlagReferanse,
                                   @JsonProperty(value = "koblingReferanse") @Valid UUID koblingReferanse,
                                   @JsonProperty(value = "arbeidsforholdInformasjon") ArbeidsforholdInformasjon arbeidsforholdInformasjon,
                                   @JsonProperty(value = "overstyrt") InntektArbeidYtelseAggregatOverstyrtDto overstyrt) {
        this.ytelseType = Objects.requireNonNull(ytelseType, "ytelseType");
        this.saksnummer = Objects.requireNonNull(saksnummer, "saksnummer");
        this.aktør = Objects.requireNonNull(aktør, "aktør");
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

    public InntektArbeidYtelseAggregatOverstyrtDto getOverstyrt() {
        return overstyrt;
    }

    public PersonIdent getAktør() {
        return this.aktør;
    }

    public YtelseType getYtelseType() {
        return this.ytelseType;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + "person=*****" + ", ytelseType=" + ytelseType + saksnummer
            + (koblingReferanse == null ? "" : ", koblingReferanse=" + koblingReferanse)
            + (grunnlagReferanse == null ? "" : ", grunnlagReferanse" + grunnlagReferanse) + (overstyrt == null ? "" : ", overstyrtIay={...}")
            + (arbeidsforholdInformasjon == null ? "" : ", arbeidsforholdInformasjon={...}") + ">";
    }


}
