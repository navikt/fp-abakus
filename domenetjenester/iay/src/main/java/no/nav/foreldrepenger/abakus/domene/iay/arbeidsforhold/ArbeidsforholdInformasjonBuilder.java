package no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold;

import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidsforholdHandlingType;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ArbeidsforholdInformasjonBuilder {

    private final ArbeidsforholdInformasjon kladd;

    private ArbeidsforholdInformasjonBuilder(ArbeidsforholdInformasjon kladd) {
        this.kladd = kladd;
    }

    public static ArbeidsforholdInformasjonBuilder oppdatere(ArbeidsforholdInformasjon oppdatere) {
        return new ArbeidsforholdInformasjonBuilder(new ArbeidsforholdInformasjon(oppdatere));
    }

    public static ArbeidsforholdInformasjonBuilder builder(Optional<ArbeidsforholdInformasjon> arbeidsforholdInformasjon) {
        var arbeidInfo = arbeidsforholdInformasjon.map(ai -> new ArbeidsforholdInformasjon(ai)).orElseGet(ArbeidsforholdInformasjon::new);
        return new ArbeidsforholdInformasjonBuilder(arbeidInfo);
    }

    public ArbeidsforholdInformasjonBuilder tilbakestillOverstyringer() {
        kladd.tilbakestillOverstyringer();
        return this;
    }

    public InternArbeidsforholdRef finnEllerOpprett(Arbeidsgiver arbeidsgiver, EksternArbeidsforholdRef eksternArbeidsforholdRef) {
        InternArbeidsforholdRef internArbeidsforholdRef = kladd.finnEllerOpprett(arbeidsgiver, eksternArbeidsforholdRef);
        if (erUkjentReferanse(arbeidsgiver, internArbeidsforholdRef)) {
            leggTilNyReferanse(new ArbeidsforholdReferanse(arbeidsgiver, internArbeidsforholdRef, eksternArbeidsforholdRef));
        }
        return internArbeidsforholdRef;
    }

    public boolean erUkjentReferanse(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef internArbeidsforholdRef) {
        return kladd.getArbeidsforholdReferanser()
            .stream()
            .noneMatch(
                referanse -> referanse.getArbeidsgiver().equals(arbeidsgiver) && referanse.getInternReferanse().equals(internArbeidsforholdRef));
    }

    public ArbeidsforholdInformasjonBuilder leggTil(ArbeidsforholdOverstyringBuilder overstyringBuilder) {
        if (!overstyringBuilder.isOppdatering()) {
            kladd.leggTilOverstyring(overstyringBuilder.build());
        }
        return this;
    }

    public ArbeidsforholdInformasjon build() {
        return kladd;
    }

    public ArbeidsforholdInformasjonBuilder fjernOverstyringVedrørende(List<ArbeidsforholdOverstyring> overstyringer) {
        overstyringer.forEach(ov -> kladd.fjernOverstyringVedrørende(ov.getArbeidsgiver(), ov.getArbeidsforholdRef()));

        return this;
    }

    public ArbeidsforholdInformasjonBuilder fjernOverstyringVedrørende(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef arbeidsforholdRef) {
        kladd.fjernOverstyringVedrørende(arbeidsgiver, arbeidsforholdRef);
        return this;
    }

    public void fjernAlleOverstyringer() {
        kladd.tilbakestillOverstyringer();
    }

    public void leggTilNyReferanse(ArbeidsforholdReferanse arbeidsforholdReferanse) {
        kladd.leggTilNyReferanse(arbeidsforholdReferanse);
    }

    public boolean kommetInntektsmeldingPåArbeidsforholdHvorViTidligereBehandletUtenInntektsmelding(Inntektsmelding inntektsmelding) {
        return kladd.getOverstyringer()
            .stream()
            .anyMatch(ov ->
                (Objects.equals(ov.getHandling(), ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING) || Objects.equals(ov.getHandling(),
                    ArbeidsforholdHandlingType.IKKE_BRUK) || Objects.equals(ov.getHandling(), ArbeidsforholdHandlingType.BRUK_MED_OVERSTYRT_PERIODE))
                    && ov.getArbeidsgiver().getErVirksomhet() && ov.getArbeidsgiver().equals(inntektsmelding.getArbeidsgiver())
                    && ov.getArbeidsforholdRef().gjelderFor(inntektsmelding.getArbeidsforholdRef()));
    }

    public Optional<Arbeidsgiver> utledeArbeidsgiverSomMåTilbakestilles(Inntektsmelding inntektsmelding) {
        if (inntektsmelding.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()) {
            return kladd.getOverstyringer()
                .stream()
                .filter(o -> o.getArbeidsgiver().equals(inntektsmelding.getArbeidsgiver()) && !o.getArbeidsforholdRef()
                    .gjelderForSpesifiktArbeidsforhold())
                .map(ArbeidsforholdOverstyring::getArbeidsgiver)
                .findFirst();
        }
        return Optional.empty();
    }

    public void fjernOverstyringerSomGjelder(Arbeidsgiver arbeidsgiver) {
        kladd.fjernOverstyringerSomGjelder(arbeidsgiver);
    }
}
