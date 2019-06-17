package no.nav.foreldrepenger.abakus.domene.iay;

import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.typer.AktørId;

public interface ByggInntektArbeidYtelseRepository {

    void lagre(KoblingReferanse koblingReferanse, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder);

    GrunnlagReferanse lagre(KoblingReferanse koblingReferanse, OppgittOpptjeningBuilder opptjeningBuilder);

    GrunnlagReferanse lagre(KoblingReferanse koblingReferanse, AktørId søkerAktørId, ArbeidsforholdInformasjonBuilder informasjon);

    GrunnlagReferanse lagre(KoblingReferanse koblingReferanse, ArbeidsforholdInformasjonBuilder informasjonBuilder, Inntektsmelding inntektsmelding);

}
