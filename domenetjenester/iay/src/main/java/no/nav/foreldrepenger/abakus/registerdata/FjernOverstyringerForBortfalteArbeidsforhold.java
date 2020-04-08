package no.nav.foreldrepenger.abakus.registerdata;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdHandlingType;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdOverstyring;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdIdentifikator;

final class FjernOverstyringerForBortfalteArbeidsforhold {

    /**
     * Identifiserer og fjerner overstyringer for arbeidsforhold som har blitt overstyrt tidligere, men som nå ikke har blitt innhentet på nytt.
     *
     * Dette kan være fordi arbeidsforholdet har blitt fjernet/endret i aareg til å ligge utenfor opplysningsperioden.
     *
     * @param grunnlagBuilder Builder for grunnlaget
     * @param innhentetArbeidsforhold Liste over arbeidsforhold som har blitt innhentet
     * @return Informasjonbuilder der overstyringer for bortfalte arbeidsforhold har blitt fjernet
     */
    static ArbeidsforholdInformasjonBuilder fjern(InntektArbeidYtelseGrunnlagBuilder grunnlagBuilder, Set<ArbeidsforholdIdentifikator> innhentetArbeidsforhold) {

        ArbeidsforholdInformasjon informasjon = grunnlagBuilder.getInformasjon();
        List<ArbeidsforholdOverstyring> overstyringerSomMåFjernes = finnOverstyringerForBortfalteArbeidsforhold(innhentetArbeidsforhold, informasjon);
        ArbeidsforholdInformasjonBuilder informasjonBuilder = grunnlagBuilder.getInformasjonBuilder();
        informasjonBuilder.fjernOverstyringVedrørende(overstyringerSomMåFjernes);
        return informasjonBuilder;
    }


    private static List<ArbeidsforholdOverstyring> finnOverstyringerForBortfalteArbeidsforhold(Set<ArbeidsforholdIdentifikator> innhentetArbeidsforhold, ArbeidsforholdInformasjon informasjon) {
        return informasjon.getOverstyringer().stream()
            .filter(FjernOverstyringerForBortfalteArbeidsforhold::erIkkeLagtTilFraInntektsmeldingEllerFiktivt)
            .filter(ov -> innhentetArbeidsforhold.stream()
                .noneMatch(arbeid -> arbeid.getArbeidsgiver().getIdentifikator().equals(ov.getArbeidsgiver().getIdentifikator())
                    && (arbeid.harArbeidsforholdRef() && arbeid.getArbeidsforholdId().gjelderFor(informasjon.finnEkstern(ov.getArbeidsgiver(), ov.getArbeidsforholdRef())))))
            .collect(Collectors.toList());
    }

    private static boolean erIkkeLagtTilFraInntektsmeldingEllerFiktivt(ArbeidsforholdOverstyring ov) {
        return !ArbeidsforholdHandlingType.BASERT_PÅ_INNTEKTSMELDING.equals(ov.getHandling())
            && !ArbeidsforholdHandlingType.LAGT_TIL_AV_SAKSBEHANDLER.equals(ov.getHandling());
    }
}
