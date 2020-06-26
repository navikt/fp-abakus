package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient;

import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SigrunSummertSkattegrunnlagResponse;

public interface SigrunConsumer {

    SigrunResponse beregnetskatt(Long aktørId);

    SigrunSummertSkattegrunnlagResponse summertSkattegrunnlag(Long aktørId);

}
