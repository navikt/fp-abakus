package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningEntitet;
import no.nav.foreldrepenger.abakus.felles.diff.DiffResult;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;

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

    Statistikk hentStats();

    Optional<OppgittOpptjeningEntitet> hentOppgittOpptjeningFor(UUID oppgittOpptjeningEksternReferanse);

    List<InntektArbeidYtelseGrunnlag> hentAlleInntektArbeidYtelseGrunnlagFor(AktørId aktørId, Saksnummer saksnummer, YtelseType ytelseType, boolean kunAktive);

    List<InntektArbeidYtelseGrunnlag> hentAlleInntektArbeidYtelseGrunnlagFor(AktørId aktørId, KoblingReferanse koblingReferanse, boolean kunAktive);

    Optional<InntektArbeidYtelseAggregatEntitet> hentIAYAggregatFor(UUID eksternReferanse);

    /**
     * Kun for migrering
     *
     * @param aktørId
     * @param saksnummer
     * @param ytelseType
     * @deprecated Kun for migrering
     */
    @Deprecated(forRemoval = true)
    void slettAltForSak(AktørId aktørId, Saksnummer saksnummer, YtelseType ytelseType);
}
