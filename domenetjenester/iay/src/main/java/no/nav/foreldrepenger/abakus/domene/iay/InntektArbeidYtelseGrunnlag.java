package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.InntektsmeldingSomIkkeKommer;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.abakus.typer.AktørId;

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
     * Returnere TRUE hvis det finnes en overstyr versjon, sjekker både FØR og ETTER
     */
    boolean harBlittSaksbehandlet();

    /**
     * Returnerer aggregat som holder alle inntektsmeldingene som benyttes i behandlingen.
     */
    Optional<InntektsmeldingAggregat> getInntektsmeldinger();

    /**
     * sjekkom bekreftet annen opptjening.  Oppgi aktørId for matchende behandling (dvs.normalt søker).
     */
    Optional<AktørArbeid> getBekreftetAnnenOpptjening(AktørId aktørId);


    /**
     * Returnerer oppgitt opptjening hvis det finnes. (Inneholder opplysninger søker opplyser om i søknaden)
     */
    Optional<OppgittOpptjening> getOppgittOpptjening();

    List<InntektsmeldingSomIkkeKommer> getInntektsmeldingerSomIkkeKommer();

    /** Unik id for dette grunnlaget. */
    Long getId();

    Optional<ArbeidsforholdInformasjon> getArbeidsforholdInformasjon();

    /** Hvorvidt dette er det siste (aktive grunnlaget) for en behandling. */
    boolean isAktiv();

    Optional<AktørArbeid> getAktørArbeidFraRegister(AktørId aktørId);

    Optional<AktørInntekt> getAktørInntektFraRegister(AktørId aktørId);

    Optional<AktørYtelse> getAktørYtelseFraRegister(AktørId aktørId);

    Collection<AktørInntekt> getAlleAktørInntektFraRegister();

    Long getKoblingId();

    GrunnlagReferanse getGrunnlagReferanse();

}