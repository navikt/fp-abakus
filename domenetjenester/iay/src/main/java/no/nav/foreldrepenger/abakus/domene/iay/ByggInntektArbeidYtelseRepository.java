package no.nav.foreldrepenger.abakus.domene.iay;

import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.abakus.typer.AktørId;

public interface ByggInntektArbeidYtelseRepository {

    /**
     * @param behandling      (Behandling)
     * @param versjonType     (REGISTER, SAKSBEHANDLET)
     * @return InntektArbeidYtelseAggregatBuilder
     * <p>
     * NB! bør benytte via InntektArbeidYtelseTjeneste og ikke direkte
     */
    InntektArbeidYtelseAggregatBuilder opprettBuilderFor(Long behandlingId, VersjonType versjonType);

    ArbeidsforholdInformasjonBuilder opprettInformasjonBuilderFor(Long behandlingId);

    void lagre(Long behandlingId, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder);

    void lagre(Long behandlingId, OppgittOpptjeningBuilder opptjeningBuilder);

    void lagre(Long behandlingId, AktørId søkerAktørId, ArbeidsforholdInformasjonBuilder informasjon);

    void kopierGrunnlagFraEksisterendeBehandling(Long fraBehandlingId, Long tilBehandlingId);

    void lagre(Long behandlingId, Inntektsmelding inntektsmelding);

}
