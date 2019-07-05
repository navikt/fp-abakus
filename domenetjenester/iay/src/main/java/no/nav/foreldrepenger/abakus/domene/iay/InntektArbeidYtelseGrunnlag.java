package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.InntektsmeldingSomIkkeKommer;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.abakus.domene.virksomhet.Virksomhet;
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

    /**
     * Returnerer en bekrefet versjon av aggregat. Denne versjon inneholder opplysninger hentet fra registere,
     * gjelder for FØR første dag i permisjonsuttaket (skjæringstidspunktet)
     */
    Optional<InntektArbeidYtelseAggregat> getOpplysningerFørSkjæringstidspunkt(LocalDate skjæringstidspunkt);

    /**
     * Returnerer en bekrefet versjon av aggregat. Denne versjon inneholder opplysninger hentet fra registere,
     * gjelder for ETTER første dag i permisjonsuttaket (skjæringstidspunktet)
     */
    Optional<InntektArbeidYtelseAggregat> getOpplysningerEtterSkjæringstidspunkt(LocalDate skjæringstidspunkt);

    /**
     * Returnere TRUE hvis det finnes en overstyr versjon, sjekker både FØR og ETTER
     */
    boolean harBlittSaksbehandlet();

    /**
     * Returnerer inntekter per aktør FØR skjæringstidspunkt (Stp). Merk at dette kan returnere for flere aktører (eks. søker + annen part)
     */
    Collection<AktørInntekt> getAktørInntektFørStp(LocalDate skjæringstidspunkt);

    /**
     * Returnere inntekter FØR skjæringstidspunkt (Stp) for angitt aktør id (hvis finnes).
     *
     */
    Optional<AktørInntekt> getAktørInntektFørStp(AktørId aktørId, LocalDate skjæringstidspunkt);

    /**
     * Returnere inntekter FØR skjæringstidspunkt (Stp) for angitt aktør id (hvis finnes).
     *
     */
    Optional<AktørInntekt> getAktørInntektEtterStp(AktørId aktørId, LocalDate skjæringstidspunkt);

    /**
     * Returnerer arbeid per aktør FØR skjæringstidspunkt (Stp). Merk at dette kan returnere for flere aktører (eks. søker + annen part)
     */
    Collection<AktørArbeid> getAktørArbeidFørStp(LocalDate skjæringstidspunkt);

    /**
     * Returnere arbeid FØR skjæringstidspunkt (Stp) for angitt aktør id (hvis finnes).
     */
    Optional<AktørArbeid> getAktørArbeidFørStp(AktørId aktørId, LocalDate skjæringstidspunkt);

    /**
     * Returnere arbeid ETTER skjæringstidspunkt (Stp) for angitt aktør id (hvis finnes).
     */
    Optional<AktørArbeid> getAktørArbeidEtterStp(AktørId aktørId, LocalDate skjæringstidspunkt);

    /**
     * Returnerer ytelser per aktør FØR skjæringstidspunkt (Stp) Merk at dette kan returnere for flere aktører (eks. søker + annen part)
     */
    Collection<AktørYtelse> getAktørYtelseFørStp(LocalDate skjæringstidspunkt);

    /**
     * Returnere ytelser FØR skjæringstidspunkt (Stp) for angitt aktør id (hvis finnes).
     */
    Optional<AktørYtelse> getAktørYtelseFørStp(AktørId aktørId, LocalDate skjæringstidspunkt);

    /**
     * Samme som getAktørYtelseFørStp(AktørId aktørId), men returnerer saksbehandlet versjon foran registerversjon om tilgjengelig
     */
    Optional<AktørYtelse> getAktørYtelseFørStpSaksBehFørReg(AktørId aktørId, LocalDate skjæringstidspunkt);

    /**
     * Returnerer alle yrkesaktivteter for en aktør FØR skjæringstidspunkt, kan velge om det skal se i bekrefet eller overstyrt
     */
    Collection<Yrkesaktivitet> hentAlleYrkesaktiviteterFørStpFor(AktørId aktørId, LocalDate skjæringstidspunkt, boolean overstyrt);

    /**
     * Returnerer aggregat som holder alle inntektsmeldingene som benyttes i behandlingen.
     */
    Optional<InntektsmeldingAggregat> getInntektsmeldinger();

    /** sjekkom bekreftet annen opptjening.  Oppgi aktørId for matchende behandling (dvs.normalt søker). */
    Optional<AktørArbeid> getBekreftetAnnenOpptjening(AktørId aktørId);

    /**
     * Returnerer oppgitt opptjening hvis det finnes. (Inneholder opplysninger søker opplyser om i søknaden)
     */
    Optional<OppgittOpptjening> getOppgittOpptjening();

    List<InntektsmeldingSomIkkeKommer> getInntektsmeldingerSomIkkeKommer();

    List<InntektsmeldingSomIkkeKommer> getInntektsmeldingerSomIkkeKommerFor(Virksomhet virksomhet);

    Long getKoblingId();

    GrunnlagReferanse getGrunnlagReferanse();
    
    /** Tidspunkt dette grunnlaget ble opprettet. (normalt lagret i databasen her). */
    LocalDateTime getOpprettetTidspunkt();

    Optional<ArbeidsforholdInformasjon> getArbeidsforholdInformasjon();

    /** Hvorvidt dette er et aktivt grunnlag (siste angitt for en koblingreferanse), eller er historisk. */
    boolean isAktiv();

}
