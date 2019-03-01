package no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag;

import java.util.List;

public interface Frilans {

    boolean getHarInntektFraFosterhjem();

    boolean getErNyoppstartet();

    boolean getHarNærRelasjon();

    List<Frilansoppdrag> getFrilansoppdrag();

}
