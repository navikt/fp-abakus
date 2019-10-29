package no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.vedtak.util.Tuple;

public class ArbeidsforholdInformasjonBuilder {

    private final ArbeidsforholdInformasjonEntitet kladd;
    private final List<Tuple<Arbeidsgiver, Tuple<InternArbeidsforholdRef, InternArbeidsforholdRef>>> erstattArbeidsforhold = new ArrayList<>();
    private final List<Tuple<Arbeidsgiver, Tuple<InternArbeidsforholdRef, InternArbeidsforholdRef>>> reverserteErstattninger = new ArrayList<>();

    private ArbeidsforholdInformasjonBuilder(ArbeidsforholdInformasjonEntitet kladd) {
        this.kladd = kladd;
    }

    public static ArbeidsforholdInformasjonBuilder oppdatere(ArbeidsforholdInformasjon oppdatere) {
        return new ArbeidsforholdInformasjonBuilder(new ArbeidsforholdInformasjonEntitet(oppdatere));
    }

    public static ArbeidsforholdInformasjonBuilder builder(Optional<ArbeidsforholdInformasjon> arbeidsforholdInformasjon) {
        var arbeidInfo = arbeidsforholdInformasjon.map(ai -> new ArbeidsforholdInformasjonEntitet(ai)).orElseGet(() -> new ArbeidsforholdInformasjonEntitet());
        return new ArbeidsforholdInformasjonBuilder(arbeidInfo);
    }

    public ArbeidsforholdOverstyringBuilder getOverstyringBuilderFor(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef ref) {
        return kladd.getOverstyringBuilderFor(arbeidsgiver, ref);
    }

    public ArbeidsforholdInformasjonBuilder tilbakestillOverstyringer() {
        final List<ArbeidsforholdReferanseEntitet> collect = kladd.getArbeidsforholdReferanser().stream().filter(it -> kladd.getOverstyringer().stream()
            .anyMatch(ov -> ov.getHandling().equals(ArbeidsforholdHandlingType.SLÅTT_SAMMEN_MED_ANNET)
                && ov.getNyArbeidsforholdRef().gjelderFor(it.getInternReferanse())))
            .collect(Collectors.toList());
        collect.forEach(it -> {
            Optional<InternArbeidsforholdRef> arbeidsforholdRef = kladd.finnForEksternBeholdHistoriskReferanse(it.getArbeidsgiver(), it.getEksternReferanse());
            if (arbeidsforholdRef.isPresent()) {
                reverserteErstattninger.add(new Tuple<>(it.getArbeidsgiver(), new Tuple<>(it.getInternReferanse(), arbeidsforholdRef.get())));
            }
        });
        kladd.tilbakestillOverstyringer();
        return this;
    }

    /**
     * Benyttes for å vite hvilke inntektsmeldinger som skal tas ut av grunnlaget ved erstatting av ny id.
     *
     * @return Liste over ArbeidsgiverEntitet / ArbeidsforholdReferanser
     */
    public List<Tuple<Arbeidsgiver, Tuple<InternArbeidsforholdRef, InternArbeidsforholdRef>>> getErstattArbeidsforhold() {
        return Collections.unmodifiableList(erstattArbeidsforhold);
    }

    /**
     * Benyttes for å vite hvilke inntektsmeldinger som skal tas ut av grunnlaget ved erstatting av ny id.
     *
     * @return Liste over ArbeidsgiverEntitet / ArbeidsforholdReferanser
     */
    public List<Tuple<Arbeidsgiver, Tuple<InternArbeidsforholdRef, InternArbeidsforholdRef>>> getReverserteErstattArbeidsforhold() {
        return Collections.unmodifiableList(reverserteErstattninger);
    }

    public InternArbeidsforholdRef finnEllerOpprett(Arbeidsgiver arbeidsgiver, EksternArbeidsforholdRef eksternArbeidsforholdRef) {
        InternArbeidsforholdRef internArbeidsforholdRef = kladd.finnEllerOpprett(arbeidsgiver, eksternArbeidsforholdRef);
        if (erUkjentReferanse(arbeidsgiver, internArbeidsforholdRef)) {
            leggTilNyReferanse(new ArbeidsforholdReferanseEntitet(arbeidsgiver, internArbeidsforholdRef, eksternArbeidsforholdRef));
        }
        return internArbeidsforholdRef;
    }

    public boolean erUkjentReferanse(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef internArbeidsforholdRef) {
        return kladd.getArbeidsforholdReferanser().stream().noneMatch(referanse -> referanse.getArbeidsgiver().equals(arbeidsgiver) && referanse.getInternReferanse().equals(internArbeidsforholdRef));
    }

    public ArbeidsforholdInformasjonBuilder erstattArbeidsforhold(Arbeidsgiver arbeidsgiver, InternArbeidsforholdRef gammelRef, InternArbeidsforholdRef ref) {
        // TODO: Sjekke om revertert allerede
        // Hvis eksisterer så reverter revertering og ikke legg inn erstattning og kall på erstatt
        erstattArbeidsforhold.add(new Tuple<>(arbeidsgiver, new Tuple<>(gammelRef, ref)));
        kladd.erstattArbeidsforhold(arbeidsgiver, gammelRef, ref);
        return this;
    }

    public ArbeidsforholdInformasjonBuilder leggTil(ArbeidsforholdOverstyringBuilder overstyringBuilder) {
        if (!overstyringBuilder.isOppdatering()) {
            kladd.leggTilOverstyring(overstyringBuilder.build());
        }
        return this;
    }

    public ArbeidsforholdInformasjonEntitet build() {
        return kladd;
    }

    public ArbeidsforholdInformasjonBuilder fjernOverstyringVedrørende(Arbeidsgiver arbeidsgiver,
                                                                       InternArbeidsforholdRef arbeidsforholdRef) {
        kladd.fjernOverstyringVedrørende(arbeidsgiver, arbeidsforholdRef);
        return this;
    }

    public void fjernAlleOverstyringer() {
        kladd.tilbakestillOverstyringer();
    }

    public void leggTilNyReferanse(ArbeidsforholdReferanseEntitet arbeidsforholdReferanse) {
        kladd.leggTilNyReferanse(arbeidsforholdReferanse);
    }

    public boolean kommetInntektsmeldingPåArbeidsforholdHvorViTidligereBehandletUtenInntektsmelding(Inntektsmelding inntektsmelding) {
        return kladd.getOverstyringer()
            .stream()
            .anyMatch(ov -> (Objects.equals(ov.getHandling(), ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING)
                || Objects.equals(ov.getHandling(), ArbeidsforholdHandlingType.IKKE_BRUK)
                || Objects.equals(ov.getHandling(), ArbeidsforholdHandlingType.BRUK_MED_OVERSTYRT_PERIODE))
                && ov.getArbeidsgiver().getErVirksomhet()
                && ov.getArbeidsgiver().equals(inntektsmelding.getArbeidsgiver())
                && ov.getArbeidsforholdRef().gjelderFor(inntektsmelding.getArbeidsforholdRef()));
    }

    public Optional<Arbeidsgiver> utledeArbeidsgiverSomMåTilbakestilles(Inntektsmelding inntektsmelding) {
        if (inntektsmelding.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()) {
            return kladd.getOverstyringer().stream()
                .filter(o -> o.getArbeidsgiver().equals(inntektsmelding.getArbeidsgiver()) &&
                    !o.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold())
                .map(ArbeidsforholdOverstyring::getArbeidsgiver)
                .findFirst();
        }
        return Optional.empty();
    }

    public void fjernOverstyringerSomGjelder(Arbeidsgiver arbeidsgiver) {
        kladd.fjernOverstyringerSomGjelder(arbeidsgiver);
    }
}
