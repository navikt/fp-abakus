package no.nav.foreldrepenger.abakus.registerdata;

import java.util.Set;

import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.registerdata.tjeneste.RegisterdataElement;

public interface IAYRegisterInnhentingTjeneste {

    boolean skalInnhenteNæringsInntekterFor(Kobling behandling);

    boolean skalInnhenteYtelseGrunnlag(Kobling kobling);

    InntektArbeidYtelseGrunnlagBuilder innhentRegisterdata(Kobling kobling, Set<RegisterdataElement> informasjonsElementer);

}
