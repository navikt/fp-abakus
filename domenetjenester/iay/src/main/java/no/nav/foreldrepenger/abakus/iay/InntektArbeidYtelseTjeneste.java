package no.nav.foreldrepenger.abakus.iay;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;

public interface InntektArbeidYtelseTjeneste {
    /**
     * @param koblingen
     * @return henter aggregat, kaster feil hvis det ikke finnes.
     */
    InntektArbeidYtelseGrunnlag hentAggregat(Kobling koblingen);

    /**
     * @param referanse
     * @return henter aggregat, kaster feil hvis det ikke finnes.
     */
    InntektArbeidYtelseGrunnlag hentAggregat(UUID referanse);

    /**
     * @param koblingId
     * @return henter optional aggregat
     */
    Optional<InntektArbeidYtelseGrunnlag> hentInntektArbeidYtelseGrunnlagForBehandling(Long koblingId);

    /**
     * @param koblingId
     * @return Register inntekt og arbeid før skjæringstidspunktet (Opprett for å endre eller legge til registeropplysning)
     */
    InntektArbeidYtelseAggregatBuilder opprettBuilderForRegister(Long koblingId);

    /**
     * @param koblingId
     * @param inntektArbeidYtelseAggregatBuilder lagrer ned aggregat (builder bestemmer hvilke del av treet som blir lagret)
     */
    void lagre(Long koblingId, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder);

    ArbeidsforholdRef finnReferanseFor(Long behandlingId, Arbeidsgiver arbeidsgiver, ArbeidsforholdRef arbeidsforholdRef, boolean beholdErstattetVerdi);

    List<Inntektsmelding> hentAlleInntektsmeldinger(Kobling behandling);

    Optional<ArbeidsforholdInformasjon> hentArbeidsforholdInformasjonForBehandling(Long behandlingId);

}
