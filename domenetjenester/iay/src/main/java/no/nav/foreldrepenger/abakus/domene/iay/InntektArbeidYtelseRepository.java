package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Optional;

import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.felles.diff.DiffResult;

public interface InntektArbeidYtelseRepository extends ByggInntektArbeidYtelseRepository {

    DiffResult diffResultat(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå, boolean kunSporedeEndringer);

    boolean erEndring(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå);

    InntektArbeidYtelseGrunnlag hentInntektArbeidYtelseForBehandling(Long behandlingId);

    Optional<InntektArbeidYtelseGrunnlag> hentInntektArbeidYtelseGrunnlagForBehandling(Long behandlingId);

    Optional<ArbeidsforholdInformasjon> hentArbeidsforholdInformasjonForBehandling(Long behandlingId);

    InntektArbeidYtelseGrunnlag hentInntektArbeidYtelseForReferanse(String referanse);
}
