package no.nav.foreldrepenger.abakus.typer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;


class InternArbeidsforholdRefTest {

    @Test
    void skal_teste_at_indexKey_kan_returneres_og_at_ref_gjelder_spesifikt_arbfor() {
        InternArbeidsforholdRef ref = InternArbeidsforholdRef.nyRef();

        assertThat(ref.getIndexKey()).isNotNull();
        assertThat(ref.gjelderForSpesifiktArbeidsforhold()).isTrue();
    }

    @Test
    void skal_teste_at_null_ref_skal_genereres() {
        InternArbeidsforholdRef ref = InternArbeidsforholdRef.nullRef();

        assertThat(ref.getUUIDReferanse()).isNull();
        assertThat(ref.gjelderForSpesifiktArbeidsforhold()).isFalse();
    }

    @Test
    void skal_teste_at_ref_med_nullref_gjelder_for_satt_referanse() {
        InternArbeidsforholdRef sattRef = InternArbeidsforholdRef.nyRef();
        InternArbeidsforholdRef nullRef = InternArbeidsforholdRef.nullRef();

        assertThat(sattRef.gjelderFor(nullRef)).isTrue();
        assertThat(nullRef.gjelderFor(sattRef)).isTrue();
    }

    @Test
    void skal_teste_at_ref_kan_lages_fra_annen_ref() {
        InternArbeidsforholdRef sattRef = InternArbeidsforholdRef.nyRef();
        InternArbeidsforholdRef nyRef = InternArbeidsforholdRef.ref(sattRef.getReferanse());

        assertThat(nyRef.getReferanse()).isNotNull();
        assertThat(nyRef.gjelderFor(sattRef)).isTrue();
    }
}
