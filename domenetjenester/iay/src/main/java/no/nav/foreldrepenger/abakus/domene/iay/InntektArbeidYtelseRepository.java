package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.felles.diff.DiffResult;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;

public interface InntektArbeidYtelseRepository extends ByggInntektArbeidYtelseRepository {

    DiffResult diffResultat(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå, boolean kunSporedeEndringer);

    GrunnlagReferanse lagre(KoblingReferanse koblingReferanse, ArbeidsforholdInformasjonBuilder informasjonBuilder, List<Inntektsmelding> inntektsmeldingerList);

    boolean erEndring(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå);

    InntektArbeidYtelseGrunnlag hentInntektArbeidYtelseForBehandling(KoblingReferanse koblingReferanse);

    Optional<InntektArbeidYtelseGrunnlag> hentInntektArbeidYtelseGrunnlagForBehandling(KoblingReferanse koblingReferanse);

    /**
     * @param nyttGrunnlag
     * @param koblingReferanse
     * @param aktiv
     * @deprecated OBS! Kun for migrering av vedtak fra FPSAK til abakus
     */
    @Deprecated(forRemoval = true)
    void lagreMigrertGrunnlag(InntektArbeidYtelseGrunnlag nyttGrunnlag, KoblingReferanse koblingReferanse, boolean aktiv);

    Optional<ArbeidsforholdInformasjon> hentArbeidsforholdInformasjonForBehandling(KoblingReferanse koblingReferanse);

    Optional<InntektArbeidYtelseGrunnlag> hentInntektArbeidYtelseForReferanse(GrunnlagReferanse grunnlagReferanse);

    Long hentKoblingIdFor(GrunnlagReferanse grunnlagReferanse);

    KoblingReferanse hentKoblingReferanseFor(GrunnlagReferanse grunnlagReferanse);

    /**
     * @param koblingReferanse
     * @param versjonType      (REGISTER, SAKSBEHANDLET)
     * @return InntektArbeidYtelseAggregatBuilder
     * <p>
     * NB! bør benytte via InntektArbeidYtelseTjeneste og ikke direkte
     */
    InntektArbeidYtelseAggregatBuilder opprettBuilderFor(KoblingReferanse koblingReferanse, UUID angittAggregatReferanse, LocalDateTime angittOpprettetTidspunkt, VersjonType versjonType);

}
