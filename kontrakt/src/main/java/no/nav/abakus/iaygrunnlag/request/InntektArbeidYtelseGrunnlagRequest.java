package no.nav.abakus.iaygrunnlag.request;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.iaygrunnlag.PersonIdent;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;

/**
 * Spesifikasjon for å hente opp et InntektArbeidYtelseGrunnlag.
 * Merk at props her kan ekskludere/kombineres.
 * Må minimum angi personident og en eller flere referanser (grunnlag, kobling, saksnummer)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, creatorVisibility = Visibility.NONE)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class InntektArbeidYtelseGrunnlagRequest {

    @JsonProperty("dataset")
    @Valid
    public Set<Dataset> dataset = EnumSet.of(Dataset.REGISTER, Dataset.OVERSTYRT);
    /**
     * Angi hvem grunnlaget hentes for.
     */
    @JsonProperty(value = "personIdent", required = true)
    @Valid
    @NotNull
    private PersonIdent person;
    @JsonProperty(value = "ytelseType")
    @Valid
    private YtelseType ytelseType;
    /**
     * Forespørsel på grunnlag referanse gir eksakt grunnlag forespurt (også utdaterte versjoner).
     */
    @JsonProperty("grunnlagReferanse")
    @Valid
    private UUID grunnlagReferanse;
    /**
     * Forespørsel på grunnlag referanse gir eksakt grunnlag forespurt (også utdaterte versjoner).
     */
    @JsonProperty("sisteKjenteGrunnlagReferanse")
    @Valid
    private UUID sisteKjenteGrunnlagReferanse;
    /**
     * Forespørsel på kobling referanse gir kun siste grunnlag på koblingen (kobling er typisk eks. behandling). Ignoreres dersom
     * grunnlagReferanse er satt.
     */
    @JsonProperty("koblingReferanse")
    @Valid
    private UUID koblingReferanse;
    /**
     * Angi evt. hvilken sak det gjelder.
     */
    @JsonProperty(value = "saksnummer")
    @Valid
    @Pattern(regexp = "^[A-Za-z0-9_\\.\\-:]+$", message = "[${validatedValue}] matcher ikke tillatt pattern '{value}'")
    private String saksnummer;
    /**
     * Hvis satt til true hentes første opprettete versjon av grunnlaget, hvis false eller ikke satt hentes den siste aktive grunnlaget.
     */
    @JsonProperty(value = "grunnlagVersjon")
    @Valid
    private GrunnlagVersjon grunnlagVersjon = GrunnlagVersjon.SISTE;

    protected InntektArbeidYtelseGrunnlagRequest() {
        // default ctor.
    }

    @JsonCreator
    public InntektArbeidYtelseGrunnlagRequest(@JsonProperty(value = "personIdent", required = true) @Valid @NotNull PersonIdent person) {
        this.person = Objects.requireNonNull(person, "person");
    }

    @AssertTrue(message = "grunnlagReferanse eller koblingReferanse eller saksnummer må spesifiseres")
    private boolean isOk() {
        return grunnlagReferanse != null || koblingReferanse != null || saksnummer != null;
    }

    public InntektArbeidYtelseGrunnlagRequest medDataset(Dataset data) {
        this.dataset.add(data);
        return this;
    }

    public InntektArbeidYtelseGrunnlagRequest medDataset(Collection<Dataset> data) {
        this.dataset = Set.copyOf(data);
        return this;
    }

    public InntektArbeidYtelseGrunnlagRequest medSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
        return this;
    }

    public InntektArbeidYtelseGrunnlagRequest medYtelseType(YtelseType ytelseType) {
        this.ytelseType = ytelseType;
        return this;
    }

    public InntektArbeidYtelseGrunnlagRequest forKobling(UUID koblingReferanse) {
        this.koblingReferanse = koblingReferanse;
        return this;
    }

    public InntektArbeidYtelseGrunnlagRequest forGrunnlag(UUID grunnlagReferanse) {
        this.grunnlagReferanse = grunnlagReferanse;
        return this;
    }

    public InntektArbeidYtelseGrunnlagRequest medSisteKjenteGrunnlagReferanse(UUID sisteKjenteGrunnlagReferanse) {
        this.sisteKjenteGrunnlagReferanse = sisteKjenteGrunnlagReferanse;
        return this;
    }

    public InntektArbeidYtelseGrunnlagRequest hentGrunnlagVersjon(GrunnlagVersjon grunnlagVersjon) {
        this.grunnlagVersjon = grunnlagVersjon;
        return this;
    }

    public Set<Dataset> getDataset() {
        return dataset;
    }

    public UUID getKoblingReferanse() {
        return koblingReferanse;
    }

    public UUID getGrunnlagReferanse() {
        return grunnlagReferanse;
    }

    public UUID getSisteKjenteGrunnlagReferanse() {
        return sisteKjenteGrunnlagReferanse;
    }

    public PersonIdent getPerson() {
        return person;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public YtelseType getYtelseType() {
        return ytelseType;
    }

    public GrunnlagVersjon getGrunnlagVersjon() {
        return grunnlagVersjon;
    }

    /**
     * Setter scope for hvilke grunnlag som hentes per behandling.
     */
    public enum GrunnlagVersjon {

        /**
         * Alle grunnlag versjoner per behandling. Egnet kun ved full migrering.
         */
        ALLE,

        /**
         * Default valg. Kun siste (dvs. aktiv) grunnlag per behandling. Typisk dersom man ønsker kun gjeldende grunnlag.
         */
        SISTE,

        /**
         * første grunnlag per behandling (kan være aktivt hvis det også er eneste), ellers er det typisk starttilstand for behandlingen.
         * Normalt vil en konsument heller være interesset i {@link #SISTE} eller {@link #FØRSTE_OG_SISTE}.
         */
        FØRSTE,

        /**
         * Både første og siste grunnlag per behandling (se #SISTE, #FØRSTE). Ignorerer mellomliggende versjoner på samme behandling. Brukes typisk
         * for å håndtere Revurdering (om det har vært endringer i behandlingen).
         */
        FØRSTE_OG_SISTE,
        ;
    }
}
