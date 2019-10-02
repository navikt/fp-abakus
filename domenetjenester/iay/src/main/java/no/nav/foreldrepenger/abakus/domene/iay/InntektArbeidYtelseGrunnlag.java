package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDateTime;
import java.util.Optional;

import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittOpptjening;

public interface InntektArbeidYtelseGrunnlag {

    /**
     * Returnerer innhentede registeropplysninger som aggregat.  Tar ikke hensyn til saksbehandlers overstyringer (se {@link #getSaksbehandletVersjon()}.
     */
    Optional<InntektArbeidYtelseAggregat> getRegisterVersjon();

    /**
     * Returnerer en overstyrt versjon av aggregat. Hvis saksbehandler har løst et aksjonspunkt i forbindele med
     * opptjening vil det finnes et overstyrt aggregat, gjelder for FØR første dag i permisjonsuttaket (skjæringstidspunktet)
     */
    Optional<InntektArbeidYtelseAggregat> getSaksbehandletVersjon();

    /** Get tidspunkt opprettet. */
    LocalDateTime getOpprettetTidspunkt();

    /**
     * Returnerer aggregat som holder alle inntektsmeldingene som benyttes i behandlingen.
     */
    Optional<InntektsmeldingAggregat> getInntektsmeldinger();

    /**
     * Returnerer oppgitt opptjening hvis det finnes. (Inneholder opplysninger søker opplyser om i søknaden)
     */
    Optional<OppgittOpptjening> getOppgittOpptjening();

    /** Unik id for dette grunnlaget. */
    Long getId();

    Optional<ArbeidsforholdInformasjon> getArbeidsforholdInformasjon();

    /** Hvorvidt dette er det siste (aktive grunnlaget) for en behandling. */
    boolean isAktiv();

    Long getKoblingId();

    GrunnlagReferanse getGrunnlagReferanse();

}
