package no.nav.abakus.iaygrunnlag.request;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.abakus.iaygrunnlag.PersonIdent;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.oppgittopptjening.v1.OppgittOpptjeningDto;

import java.util.Objects;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
public class OppgittOpptjeningMottattRequest {

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

    @JsonProperty(value = "oppgittOpptjening", required = true)
    @NotNull
    @Valid
    private OppgittOpptjeningDto oppgittOpptjening;

    /**
     * Optional - for now, gjør required når K9, FP sender.
     */
    @JsonProperty(value = "ytelseType")
    private YtelseType ytelseType = YtelseType.UDEFINERT;

    @JsonCreator
    public OppgittOpptjeningMottattRequest(@JsonProperty(value = "saksnummer", required = true) @Valid @NotNull String saksnummer,
                                           @JsonProperty(value = "koblingReferanse", required = true) @Valid @NotNull UUID koblingReferanse,
                                           @JsonProperty(value = "aktør", required = true) @NotNull @Valid PersonIdent aktør,
                                           @JsonProperty(value = "ytelseType") YtelseType ytelseType,
                                           @JsonProperty(value = "oppgittOpptjening", required = true) @NotNull @Valid OppgittOpptjeningDto oppgittOpptjening) {
        this.saksnummer = saksnummer;
        this.koblingReferanse = koblingReferanse;
        this.aktør = aktør;
        this.ytelseType = ytelseType;
        this.oppgittOpptjening = oppgittOpptjening;
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

    public OppgittOpptjeningDto getOppgittOpptjening() {
        return oppgittOpptjening;
    }

    public YtelseType getYtelseType() {
        return this.ytelseType;
    }

    public boolean harOppgittJournalpostId() {
        return oppgittOpptjening.getJournalpostId() != null;
    }

    public boolean harOppgittInnsendingstidspunkt() {
        return oppgittOpptjening.getInnsendingstidspunkt() != null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(saksnummer, koblingReferanse, aktør, ytelseType);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof OppgittOpptjeningMottattRequest)) {
            return false;
        }
        var other = (OppgittOpptjeningMottattRequest) obj;
        return Objects.equals(saksnummer, other.saksnummer) && Objects.equals(koblingReferanse, other.koblingReferanse) && Objects.equals(aktør,
            other.aktør) && Objects.equals(ytelseType, other.ytelseType);

    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<saksnummer=" + saksnummer + ", koblingRef=" + koblingReferanse + ", ytelseType=" + ytelseType + ">";
    }
}
