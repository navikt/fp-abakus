package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun;

import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.typer.AktørId;

public interface SigrunTjeneste {

    void hentOgLagrePGI(Kobling behandling, AktørId aktørId);
}
