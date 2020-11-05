package no.nav.foreldrepenger.abakus.iay.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.abakus.domene.virksomhet.Virksomhet;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;

public class InntektsmeldingDiffTjenesteTest {
    private static final Arbeidsgiver AG1 = Arbeidsgiver.virksomhet(lagVirksomhet("910909088"));

    private static Virksomhet lagVirksomhet(String orgnr) {
        Virksomhet.Builder builder = new Virksomhet.Builder();
        return builder.medOrgnr(orgnr)
            .medNavn("Test")
            .medOppstart(LocalDate.of(2010,1,1))
            .build();
    }

    @Test
    public void skal_ikke_finne_diff_ved_like_inntektsmeldinger() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime innsending = LocalDateTime.now();
        LocalDate mottatt = LocalDate.now();
        Inntektsmelding inntektsmelding = lagIMf(AG1, 35_000, 35_000, uuid, innsending, mottatt);
        Map<Inntektsmelding, ArbeidsforholdInformasjon> førsteMap = new HashMap<>();
        Map<Inntektsmelding, ArbeidsforholdInformasjon> andreMap = new HashMap<>();
        førsteMap.put(inntektsmelding, new ArbeidsforholdInformasjon());
        andreMap.put(inntektsmelding, new ArbeidsforholdInformasjon());
        Map<Inntektsmelding, ArbeidsforholdInformasjon> diff = InntektsmeldingDiffTjeneste.utledDifferanseIInntektsmeldinger(førsteMap, andreMap);
        assertThat(diff).isEmpty();
    }

    @Test
    public void skal_lage_diff_tilkommet_im() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime innsending = LocalDateTime.now();
        LocalDate mottatt = LocalDate.now();
        Inntektsmelding inntektsmelding = lagIMf(AG1, 35_000, 35_000, uuid, innsending, mottatt);
        Map<Inntektsmelding, ArbeidsforholdInformasjon> førsteMap = new HashMap<>();
        Map<Inntektsmelding, ArbeidsforholdInformasjon> andreMap = new HashMap<>();
        førsteMap.put(inntektsmelding, new ArbeidsforholdInformasjon());
        Map<Inntektsmelding, ArbeidsforholdInformasjon> diff = InntektsmeldingDiffTjeneste.utledDifferanseIInntektsmeldinger(førsteMap, andreMap);
        assertThat(diff.keySet()).hasSize(1);
        assertThat(diff.keySet()).contains(inntektsmelding);
    }

    @Test
    public void skal_lage_diff_ny_im_med_nytt_refusjonakrav() {
        UUID uuid = UUID.randomUUID();
        LocalDateTime innsending = LocalDateTime.now();
        LocalDate mottatt = LocalDate.now();
        Inntektsmelding inntektsmelding = lagIMf(AG1, 35_000, 0, uuid, innsending, mottatt);
        Inntektsmelding inntektsmelding2 = lagIMf(AG1, 35_000, 35_000, UUID.randomUUID(), innsending.plusDays(1), mottatt.plusDays(1));
        Map<Inntektsmelding, ArbeidsforholdInformasjon> førsteMap = new HashMap<>();
        Map<Inntektsmelding, ArbeidsforholdInformasjon> andreMap = new HashMap<>();
        andreMap.put(inntektsmelding, new ArbeidsforholdInformasjon());
        førsteMap.put(inntektsmelding, new ArbeidsforholdInformasjon());
        førsteMap.put(inntektsmelding2, new ArbeidsforholdInformasjon());
        Map<Inntektsmelding, ArbeidsforholdInformasjon> diff = InntektsmeldingDiffTjeneste.utledDifferanseIInntektsmeldinger(førsteMap, andreMap);
        assertThat(diff.keySet()).hasSize(1);
        assertThat(diff.keySet()).contains(inntektsmelding2);
    }

    private Inntektsmelding lagIMf(Arbeidsgiver ag, int inntekt, int refusjon, UUID uuid,
                                   LocalDateTime innsendingstidspunkt,
                                   LocalDate motattdato) {
        return InntektsmeldingBuilder.builder()
            .medJournalpostId(uuid.toString())
            .medArbeidsgiver(ag)
            .medBeløp(BigDecimal.valueOf(inntekt))
            .medRefusjon(BigDecimal.valueOf(refusjon))
            .medArbeidsforholdId(InternArbeidsforholdRef.ref(uuid))
            .medMottattDato(motattdato)
            .medInnsendingstidspunkt(innsendingstidspunkt)
            .build();
    }
}
