package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient;

import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.summertskattegrunnlag.SigrunSummertSkattegrunnlagResponse;

public interface SigrunConsumer {

    SigrunResponse beregnetskatt(Long aktørId, IntervallEntitet opplysningsperiode);

    SigrunSummertSkattegrunnlagResponse summertSkattegrunnlag(Long aktørId, IntervallEntitet opplysningsperiode);

}
