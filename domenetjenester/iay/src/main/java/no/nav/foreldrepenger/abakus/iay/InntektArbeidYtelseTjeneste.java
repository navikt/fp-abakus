package no.nav.foreldrepenger.abakus.iay;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.Yrkesaktivitet;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.InntektsmeldingSomIkkeKommer;
import no.nav.foreldrepenger.abakus.domene.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;

public interface InntektArbeidYtelseTjeneste {
    /**
     * @param behandling
     * @return henter aggregat, kaster feil hvis det ikke finnes.
     */
    InntektArbeidYtelseGrunnlag hentAggregat(Kobling behandling);

    /**
     * @param behandling
     * @return henter optional aggregat
     */
    Optional<InntektArbeidYtelseGrunnlag> hentInntektArbeidYtelseGrunnlagForBehandling(Long behandlingId);

    /**
     * @param behandling
     * @return Register inntekt og arbeid før skjæringstidspunktet (Opprett for å endre eller legge til registeropplysning)
     */
    InntektArbeidYtelseAggregatBuilder opprettBuilderForRegister(Long behandlingId);

    /**
     * @param behandling
     * @return Saksbehanldet inntekt og arbeid før skjæringstidspunktet (Opprett for å endre eller legge til saksbehanldet)
     */
    InntektArbeidYtelseAggregatBuilder opprettBuilderForSaksbehandlet(Long behandlingId);

    /**
     * @param behandling
     * @param inntektArbeidYtelseAggregatBuilder lagrer ned aggregat (builder bestemmer hvilke del av treet som blir lagret)
     */
    void lagre(Long behandlingId, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder);

    Map<String, Set<String>> utledManglendeInntektsmeldingerFraArkiv(Kobling behandling);

    Map<String, Set<String>> utledManglendeInntektsmeldingerFraGrunnlag(Kobling behandling);

    Collection<Yrkesaktivitet> hentYrkesaktiviteterForSøker(Kobling behandling, boolean overstyrt);

    InntektArbeidYtelseGrunnlag hentFørsteVersjon(Kobling behandling);

    ArbeidsforholdRef finnReferanseFor(Long behandlingId, Arbeidsgiver arbeidsgiver, ArbeidsforholdRef arbeidsforholdRef, boolean beholdErstattetVerdi);

    boolean søkerHarOppgittEgenNæring(Kobling behandling);

    List<Inntektsmelding> hentAlleInntektsmeldinger(Kobling behandling);

    List<InntektsmeldingSomIkkeKommer> hentAlleInntektsmeldingerSomIkkeKommer(Long behandlingId);

    Optional<InntektArbeidYtelseGrunnlag> hentForrigeVersjonAvInntektsmeldingForBehandling(Long behandlingId);

    Optional<ArbeidsforholdInformasjon> hentArbeidsforholdInformasjonForBehandling(Long behandlingId);

    Optional<ArbeidsforholdInformasjon> hentArbeidsforholdInformasjonForGrunnlag(Long inntektArbeidYtelseGrunnlagId);
}
