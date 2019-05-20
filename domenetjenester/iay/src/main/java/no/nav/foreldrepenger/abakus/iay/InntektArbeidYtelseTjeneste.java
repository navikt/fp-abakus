package no.nav.foreldrepenger.abakus.iay;


import java.util.Optional;

import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;

public interface InntektArbeidYtelseTjeneste {
    /**
     * @param koblingen
     * @return henter aggregat, kaster feil hvis det ikke finnes.
     */
    InntektArbeidYtelseGrunnlag hentAggregat(KoblingReferanse koblingReferanse);

    /**
     * @param referanse - unik referanse for aggregat
     * @return henter aggregat, kaster feil hvis det ikke finnes.
     */
    InntektArbeidYtelseGrunnlag hentAggregat(GrunnlagReferanse referanse);

    /**
     * @param referanse (ekstern referanse for kobling (eks. behandlingUuid)).
     * @return henter koblingen grunnlagsreferansen er koblet til.
     */
    Long hentKoblingIdFor(GrunnlagReferanse grunnlagReferanse);

    /**
     * @param koblingReferanse
     * @return henter optional aggregat
     */
    Optional<InntektArbeidYtelseGrunnlag> hentGrunnlagFor(KoblingReferanse koblingReferanse);

    /**
     * @param grunnlagReferanse
     * @return henter optional aggregat
     */
    Optional<InntektArbeidYtelseGrunnlag> hentGrunnlagFor(GrunnlagReferanse grunnlagReferanse);
    
    /**
     * @param koblingReferanse
     * @return Register inntekt og arbeid før skjæringstidspunktet (Opprett for å endre eller legge til registeropplysning)
     */
    InntektArbeidYtelseAggregatBuilder opprettBuilderForRegister(KoblingReferanse koblingReferanse);

    /**
     * @param koblingReferanse
     * @return Saksbehandlers overstyringer av IAY (primært {@link no.nav.foreldrepenger.abakus.domene.iay.AktørArbeid}).
     */
    InntektArbeidYtelseAggregatBuilder opprettBuilderForSaksbehandlerOverstyring(KoblingReferanse koblingReferanse);
    
    /**
     * @param koblingId
     * @param inntektArbeidYtelseAggregatBuilder lagrer ned aggregat (builder bestemmer hvilke del av treet som blir lagret)
     */
    void lagre(KoblingReferanse koblingReferanse, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder);

    void lagre(KoblingReferanse koblingReferanse, AktørId aktørId, ArbeidsforholdInformasjonBuilder builder);

    ArbeidsforholdRef finnReferanseFor(KoblingReferanse koblingReferanse, Arbeidsgiver arbeidsgiver, ArbeidsforholdRef arbeidsforholdRef, boolean beholdErstattetVerdi);

    KoblingReferanse hentKoblingReferanse(GrunnlagReferanse grunnlagReferanse);

    ArbeidsforholdInformasjon hentArbeidsforholdInformasjonForKobling(KoblingReferanse koblingReferanse);




}
