package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Collection;
import java.util.List;

import no.nav.foreldrepenger.abakus.typer.AktørId;

public interface AktørInntekt {

    /**
     * Aktøren inntekten er relevant for
     *
     * @return aktørid
     */
    AktørId getAktørId();

    /**
     * Inntekter fra SIGRUN. Inneholder kun beregnet skatt
     *
     * @return Liste med inntekter per arbeidsgiver
     */
    List<Inntekt> getBeregnetSkatt();

    /**
     * Inntekter fra inntektskomponenten. Inneholder kun pensjonsgivende inntekt
     *
     * @return Liste med inntekter per arbeidsgiver
     */
    List<Inntekt> getInntektPensjonsgivende();

    /**
     * Inntekter fra inntektskomponenten. Inneholder kun inntekter som er relevant for beregningsgrunnlaget
     *
     * @return Liste med inntekter per arbeidsgiver
     */
    List<Inntekt> getInntektBeregningsgrunnlag();

    /**
     * Inntekter fra inntektskomponenten. Inneholder kun inntekter som er relevant for sammenligningsgrunnlaget
     *
     * @return Liste med inntekter per arbeidsgiver
     */
    List<Inntekt> getInntektSammenligningsgrunnlag();

    Long getId();

    /** Returner alle inntekter, ufiltrert. */
    Collection<Inntekt> getInntekt();
}
