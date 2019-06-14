package no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
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
    
    public static ArbeidsforholdInformasjonBuilder builder(Optional<ArbeidsforholdInformasjon> arbeidsforholdInformasjon) {
        var arbeidInfo = arbeidsforholdInformasjon.map(ai -> new ArbeidsforholdInformasjonEntitet(ai)).orElseGet(() -> new ArbeidsforholdInformasjonEntitet());
        return new ArbeidsforholdInformasjonBuilder(arbeidInfo);
    }

}
