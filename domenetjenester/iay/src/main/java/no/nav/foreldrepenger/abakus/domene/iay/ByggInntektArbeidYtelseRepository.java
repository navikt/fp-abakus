package no.nav.foreldrepenger.abakus.domene.iay;

import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.typer.AktørId;

public interface ByggInntektArbeidYtelseRepository {

    /**
     * @param behandling      (Behandling)
     * @param versjonType     (REGISTER, SAKSBEHANDLET)
     * @return InntektArbeidYtelseAggregatBuilder
     * <p>
     * NB! bør benytte via InntektArbeidYtelseTjeneste og ikke direkte
     */
    InntektArbeidYtelseAggregatBuilder opprettBuilderFor(KoblingReferanse koblingReferanse, VersjonType versjonType);

    void lagre(KoblingReferanse koblingReferanse, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder);

    void lagre(KoblingReferanse koblingReferanse, OppgittOpptjeningBuilder opptjeningBuilder);

    void lagre(KoblingReferanse koblingReferanse, AktørId søkerAktørId, ArbeidsforholdInformasjonBuilder informasjon);

    void lagre(KoblingReferanse koblingReferanse, Inntektsmelding inntektsmelding);

}
