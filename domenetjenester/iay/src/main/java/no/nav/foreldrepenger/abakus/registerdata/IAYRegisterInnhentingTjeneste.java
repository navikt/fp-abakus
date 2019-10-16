package no.nav.foreldrepenger.abakus.registerdata;

import java.util.Set;

import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.registerdata.tjeneste.RegisterdataElement;

public interface IAYRegisterInnhentingTjeneste {

    boolean skalInnhenteNæringsInntekterFor(Kobling behandling);

    boolean skalInnhenteYtelseGrunnlag(Kobling kobling);

    InntektArbeidYtelseAggregatBuilder innhentRegisterdata(Kobling kobling, Set<RegisterdataElement> informasjonsElementer);

}
