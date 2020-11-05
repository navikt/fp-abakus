package no.nav.foreldrepenger.abakus.domene.iay;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.abakus.iaygrunnlag.request.Dataset;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;

public class InntektArbeidYtelseGrunnlagBuilderTest {

    @Test
    public void kopier_deler_test() {
        final var gr = new InntektArbeidYtelseGrunnlag(UUID.randomUUID(), LocalDateTime.now());
        gr.setOppgittOpptjening(OppgittOpptjeningBuilder.ny().build());
        gr.setOverstyrtOppgittOpptjening(OppgittOpptjeningBuilder.ny().build());
        gr.setRegister(new InntektArbeidYtelseAggregat());
        gr.setSaksbehandlet(new InntektArbeidYtelseAggregat());
        gr.setInformasjon(new ArbeidsforholdInformasjon());
        gr.setInntektsmeldinger(new InntektsmeldingAggregat());

        assertThat(gr.getOppgittOpptjening()).isPresent();
        assertThat(gr.getRegisterVersjon()).isPresent();
        assertThat(gr.getSaksbehandletVersjon()).isPresent();
        assertThat(gr.getInntektsmeldinger()).isPresent();

        var nyttGrunnlag = InntektArbeidYtelseGrunnlagBuilder.kopierDeler(gr, EnumSet.of(Dataset.OPPGITT_OPPTJENING, Dataset.OVERSTYRT_OPPGITT_OPPTJENING, Dataset.INNTEKTSMELDING, Dataset.REGISTER, Dataset.OVERSTYRT)).build();

        assertThat(nyttGrunnlag.getOppgittOpptjening()).isPresent();
        assertThat(nyttGrunnlag.getOverstyrtOppgittOpptjening()).isPresent();
        assertThat(nyttGrunnlag.getRegisterVersjon()).isPresent();
        assertThat(nyttGrunnlag.getSaksbehandletVersjon()).isPresent();
        assertThat(nyttGrunnlag.getInntektsmeldinger()).isPresent();

        nyttGrunnlag = InntektArbeidYtelseGrunnlagBuilder.kopierDeler(gr, EnumSet.of(Dataset.INNTEKTSMELDING, Dataset.REGISTER, Dataset.OVERSTYRT)).build();

        assertThat(nyttGrunnlag.getOppgittOpptjening()).isNotPresent();
        assertThat(nyttGrunnlag.getOverstyrtOppgittOpptjening()).isNotPresent();
        assertThat(nyttGrunnlag.getRegisterVersjon()).isPresent();
        assertThat(nyttGrunnlag.getSaksbehandletVersjon()).isPresent();
        assertThat(nyttGrunnlag.getInntektsmeldinger()).isPresent();

        nyttGrunnlag = InntektArbeidYtelseGrunnlagBuilder.kopierDeler(gr, EnumSet.of(Dataset.REGISTER, Dataset.OVERSTYRT)).build();

        assertThat(nyttGrunnlag.getOppgittOpptjening()).isNotPresent();
        assertThat(nyttGrunnlag.getOverstyrtOppgittOpptjening()).isNotPresent();
        assertThat(nyttGrunnlag.getRegisterVersjon()).isPresent();
        assertThat(nyttGrunnlag.getSaksbehandletVersjon()).isPresent();
        assertThat(nyttGrunnlag.getInntektsmeldinger()).isNotPresent();

        nyttGrunnlag = InntektArbeidYtelseGrunnlagBuilder.kopierDeler(gr, EnumSet.of(Dataset.REGISTER)).build();

        assertThat(nyttGrunnlag.getOppgittOpptjening()).isNotPresent();
        assertThat(nyttGrunnlag.getOverstyrtOppgittOpptjening()).isNotPresent();
        assertThat(nyttGrunnlag.getRegisterVersjon()).isPresent();
        assertThat(nyttGrunnlag.getSaksbehandletVersjon()).isNotPresent();
        assertThat(nyttGrunnlag.getInntektsmeldinger()).isNotPresent();

        nyttGrunnlag = InntektArbeidYtelseGrunnlagBuilder.kopierDeler(gr, Set.of()).build();

        assertThat(nyttGrunnlag.getOppgittOpptjening()).isNotPresent();
        assertThat(nyttGrunnlag.getOverstyrtOppgittOpptjening()).isNotPresent();
        assertThat(nyttGrunnlag.getRegisterVersjon()).isNotPresent();
        assertThat(nyttGrunnlag.getSaksbehandletVersjon()).isNotPresent();
        assertThat(nyttGrunnlag.getInntektsmeldinger()).isNotPresent();
    }
}
