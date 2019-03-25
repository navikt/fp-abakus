package no.nav.foreldrepenger.abakus.iay.tjeneste.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.ArbeidsgiverDto;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class ReferanseForArbeidsforhold implements AbacDto {

    @NotNull
    @Valid
    private ReferanseDto referanse; // kobling-referanse
    @NotNull
    @Valid
    private ArbeidsgiverDto arbeidsgiver;
    @Pattern(regexp = ".*")
    private String arbeidsforholdId;

    public ReferanseForArbeidsforhold() {
    }

    public ReferanseDto getReferanse() {
        return referanse;
    }

    public void setReferanse(ReferanseDto referanse) {
        this.referanse = referanse;
    }

    public ArbeidsgiverDto getArbeidsgiver() {
        return arbeidsgiver;
    }

    public void setArbeidsgiver(ArbeidsgiverDto arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    public String getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public void setArbeidsforholdId(String arbeidsforholdId) {
        this.arbeidsforholdId = arbeidsforholdId;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett();
    }
}
