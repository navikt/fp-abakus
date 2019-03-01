package no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag;

import java.util.List;
import java.util.Optional;

public interface OppgittOpptjening {

    List<OppgittArbeidsforhold> getOppgittArbeidsforhold();

    List<EgenNæring> getEgenNæring();

    List<AnnenAktivitet> getAnnenAktivitet();

    Optional<Frilans> getFrilans();
}
