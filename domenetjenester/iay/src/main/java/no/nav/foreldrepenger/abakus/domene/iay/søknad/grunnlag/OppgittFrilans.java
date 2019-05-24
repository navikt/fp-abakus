package no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag;

import java.util.List;

public interface OppgittFrilans {

    boolean getHarInntektFraFosterhjem();

    boolean getErNyoppstartet();

    boolean getHarNærRelasjon();

    List<OppgittFrilansoppdrag> getFrilansoppdrag();

}
