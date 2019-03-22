package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.arbeidsforhold;

import java.util.List;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.ArbeidsforholdRefDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.ArbeidsgiverDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.PeriodeDto;

public class ArbeidsforholdDto {

    private ArbeidsgiverDto arbeidsgiver;
    private ArbeidsforholdRefDto arbeidsforholdId;
    private ArbeidType type;
    private List<PeriodeDto> ansettelsesperiode;

    public ArbeidsforholdDto() {
    }

    public ArbeidsgiverDto getArbeidsgiver() {
        return arbeidsgiver;
    }

    public void setArbeidsgiver(ArbeidsgiverDto arbeidsgiver) {
        this.arbeidsgiver = arbeidsgiver;
    }

    public ArbeidsforholdRefDto getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public void setArbeidsforholdId(ArbeidsforholdRefDto arbeidsforholdId) {
        this.arbeidsforholdId = arbeidsforholdId;
    }

    public ArbeidType getType() {
        return type;
    }

    public void setType(ArbeidType type) {
        this.type = type;
    }

    public List<PeriodeDto> getAnsettelsesperiode() {
        return ansettelsesperiode;
    }

    public void setAnsettelsesperiode(List<PeriodeDto> ansettelsesperiode) {
        this.ansettelsesperiode = ansettelsesperiode;
    }
}
