package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.diff.DiffResult;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;

public interface InntektArbeidYtelseRepository extends ByggInntektArbeidYtelseRepository {

    DiffResult diffResultat(InntektArbeidYtelseGrunnlagEntitet før, InntektArbeidYtelseGrunnlagEntitet nå, boolean kunSporedeEndringer);

    boolean erEndring(Kobling behandling, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder);

    boolean erEndringPåInntektsmelding(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå);

    boolean erEndringPåAktørArbeid(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå, LocalDate skjæringstidspunkt);

    boolean erEndringPåAktørInntekt(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå, LocalDate skjæringstidspunkt);

    AktørYtelseEndring endringPåAktørYtelse(Saksnummer egetSaksnummer, InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå, LocalDate skjæringstidspunkt);

    boolean erEndring(Kobling behandling, Kobling nyBehandling);

    boolean erEndring(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå);

    InntektArbeidYtelseGrunnlag hentFørsteVersjon(Long behandlingId);

    InntektArbeidYtelseGrunnlag hentInntektArbeidYtelseForBehandling(Long behandlingId);

    Optional<InntektArbeidYtelseGrunnlag> hentInntektArbeidYtelseGrunnlagForBehandling(Long behandlingId);

    boolean harArbeidsforholdMedArbeidstyperSomAngitt(Long behandlingId, AktørId aktørId, Set<ArbeidType> angitteArbeidtyper, LocalDate skjæringstidspunkt);

    Optional<Long> hentIdPåAktivInntektArbeidYtelseForBehandling(Long behandlingId);

    InntektArbeidYtelseGrunnlag hentInntektArbeidYtelseForGrunnlagId(Long aggregatId);

    Optional<ArbeidsforholdInformasjon> hentArbeidsforholdInformasjonForBehandling(Long behandlingId);

    Optional<ArbeidsforholdInformasjon> hentArbeidsforholdInformasjonForGrunnlagId(Long inntektArbeidYtelseGrunnlagId);

    Optional<InntektArbeidYtelseGrunnlag> hentForrigeVersjonAvInntektsmeldingForBehandling(Long behandlingId);
}
