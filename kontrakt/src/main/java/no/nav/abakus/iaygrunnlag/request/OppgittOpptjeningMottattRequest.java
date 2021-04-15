package no.nav.abakus.iaygrunnlag.request;

import java.util.Objects;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.iaygrunnlag.PersonIdent;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.iaygrunnlag.oppgittopptjening.v1.OppgittOpptjeningDto;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
public class OppgittOpptjeningMottattRequest {

    @JsonProperty(value = "opptjeningPrJournalpostId")
    private Boolean opptjeningPrJournalpostId;

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
    public OppgittOpptjeningMottattRequest(@JsonProperty(value = "opptjeningPrJournalpostId") Boolean opptjeningPrJournalpostId,
                                           @JsonProperty(value = "saksnummer", required = true) @Valid @NotNull String saksnummer,
                                           @JsonProperty(value = "koblingReferanse", required = true) @Valid @NotNull UUID koblingReferanse,
                                           @JsonProperty(value = "aktør", required = true) @NotNull @Valid PersonIdent aktør,
                                           @JsonProperty(value = "ytelseType") YtelseType ytelseType,
                                           @JsonProperty(value = "oppgittOpptjening", required = true) @NotNull @Valid OppgittOpptjeningDto oppgittOpptjening) {
        this.opptjeningPrJournalpostId = opptjeningPrJournalpostId;
        this.saksnummer = saksnummer;
        this.koblingReferanse = koblingReferanse;
        this.aktør = aktør;
        this.ytelseType = ytelseType;
        this.oppgittOpptjening = oppgittOpptjening;
    }

    @AssertFalse(message = "Når flagget opptjeningPrJournalpostId er satt, må journalpostId settes i oppgittOpptjening")
    private boolean isManglerJournalpost() {
        return Boolean.TRUE.equals(opptjeningPrJournalpostId) && oppgittOpptjening.getJournalpostId() == null;
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

    public boolean erOpptjeningPrJournalpostId() {
        return Boolean.TRUE.equals(opptjeningPrJournalpostId);
    }

    public void setOpptjeningPrJournalpostId(Boolean opptjeningPrJournalpostId) {
        this.opptjeningPrJournalpostId = opptjeningPrJournalpostId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(saksnummer, koblingReferanse, aktør, ytelseType);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof OppgittOpptjeningMottattRequest))
            return false;
        var other = (OppgittOpptjeningMottattRequest) obj;
        return Objects.equals(saksnummer, other.saksnummer)
            && Objects.equals(koblingReferanse, other.koblingReferanse)
            && Objects.equals(aktør, other.aktør)
            && Objects.equals(ytelseType, other.ytelseType);

    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<saksnummer=" + saksnummer
            + ", koblingRef=" + koblingReferanse
            + ", ytelseType=" + ytelseType
            + ">";
    }
}
