package no.nav.foreldrepenger.abakus.registerdata;

import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.typer.AktørId;

public interface IAYRegisterInnhentingTjeneste {

    InntektArbeidYtelseAggregatBuilder innhentInntekterFor(Kobling behandling, AktørId aktørId, InntektsKilde... kilder);

    boolean skalInnhenteNæringsInntekterFor(Kobling behandling);

    boolean skalInnhenteYtelseGrunnlag(Kobling kobling);

    InntektArbeidYtelseAggregatBuilder innhentRegisterdata(Kobling kobling);

}
