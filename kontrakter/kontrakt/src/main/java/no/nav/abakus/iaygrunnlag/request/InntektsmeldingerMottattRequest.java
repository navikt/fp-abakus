package no.nav.abakus.iaygrunnlag.request;

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
import no.nav.abakus.iaygrunnlag.inntektsmelding.v1.InntektsmeldingerDto;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
public class InntektsmeldingerMottattRequest {
    @JsonProperty(value = "saksnummer", required = true)
    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9_\\.\\-:]+$", message = "[${validatedValue}] matcher ikke tillatt pattern '{value}'")
    @Valid
    private String saksnummer;

    @JsonProperty(value = "koblingReferanse", required = true)
    @NotNull
    @Valid
    private UUID koblingReferanse;

    @JsonProperty(value = "aktør", required = true)
    @NotNull
    @Valid
    private PersonIdent aktør;

    @JsonProperty(value = "inntektsmeldinger", required = true)
    @NotNull
    @Valid
    private InntektsmeldingerDto inntektsmeldinger;

    @JsonProperty(value = "ytelseType")
    @NotNull
    @Valid
    private YtelseType ytelseType = YtelseType.UDEFINERT;

    @JsonCreator
    public InntektsmeldingerMottattRequest(@JsonProperty(value = "saksnummer", required = true) @Valid @NotNull String saksnummer,
                                           @JsonProperty(value = "koblingReferanse", required = true) @Valid @NotNull UUID koblingReferanse,
                                           @JsonProperty(value = "aktør", required = true) @NotNull @Valid PersonIdent aktør,
                                           @JsonProperty(value = "ytelseType") @NotNull @Valid YtelseType ytelseType,
                                           @JsonProperty(value = "inntektsmeldinger", required = true) @NotNull @Valid InntektsmeldingerDto inntektsmeldinger) {
        this.saksnummer = saksnummer;
        this.koblingReferanse = koblingReferanse;
        this.aktør = aktør;
        this.ytelseType = ytelseType;
        this.inntektsmeldinger = inntektsmeldinger;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public UUID getKoblingReferanse() {
        return koblingReferanse;
    }

    public PersonIdent getAktør() {
        return aktør;
    }

    public InntektsmeldingerDto getInntektsmeldinger() {
        return inntektsmeldinger;
    }

    public YtelseType getYtelseType() {
        return this.ytelseType;
    }
}
