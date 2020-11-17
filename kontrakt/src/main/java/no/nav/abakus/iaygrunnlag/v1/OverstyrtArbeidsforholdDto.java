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
public class OverstyrtArbeidsforholdDto {

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
     * Original grunnlagreferanse - refererer tidligere versjon av grunnlag hvis dette skal overstyre eksisterende data.
     */
    @JsonProperty(value = "originalGrunnlagReferanse")
    @Valid
    private UUID originalGrunnlagReferanse;

    /** Referanser til arbeidsforhold satt av saksbehandler. */
    @JsonProperty(value = "arbeidsforholdInformasjon")
    @Valid
    private ArbeidsforholdInformasjon arbeidsforholdInformasjon;

    @JsonCreator
    public OverstyrtArbeidsforholdDto(@JsonProperty(value = "personIdent", required = true) PersonIdent person,
                                           @JsonProperty(value = "originalGrunnlagReferanse") @Valid @NotNull UUID originalGrunnlagReferanse,
                                           @JsonProperty(value = "koblingReferanse") @Valid @NotNull UUID koblingReferanse,
                                           @JsonProperty(value = "ytelseType") YtelseType ytelseType,
                                           @JsonProperty(value = "arbeidsforholdInformasjon") ArbeidsforholdInformasjon arbeidsforholdInformasjon) {
        this.ytelseType = Objects.requireNonNull(ytelseType, "ytelseType");
        this.person = Objects.requireNonNull(person, "person");
        if (koblingReferanse == null && originalGrunnlagReferanse == null) {
            throw new IllegalArgumentException("Må oppgi minst en av koblingReferanse, originalGrunnlagReferanse");
        }
        this.originalGrunnlagReferanse = originalGrunnlagReferanse;
        this.koblingReferanse = koblingReferanse;

        this.arbeidsforholdInformasjon = arbeidsforholdInformasjon;
    }

    public ArbeidsforholdInformasjon getArbeidsforholdInformasjon() {
        return arbeidsforholdInformasjon;
    }

    public UUID getOriginalGrunnlagReferanse() {
        return originalGrunnlagReferanse;
    }

    public UUID getKoblingReferanse() {
        return koblingReferanse;
    }

    public PersonIdent getPerson() {
        return this.person;
    }

    public YtelseType getYtelseType() {
        return this.ytelseType;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<"
            + "person=*****"
            + ", ytelseType=" + ytelseType
            + (koblingReferanse == null ? "" : ", koblingReferanse=" + koblingReferanse)
            + (originalGrunnlagReferanse == null ? "" : ", grunnlagReferanse" + originalGrunnlagReferanse)
            + (arbeidsforholdInformasjon == null ? "" : ", arbeidsforholdInformasjon={...}")
            + ">";
    }

}
