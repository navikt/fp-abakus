package no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OppgittOpptjening {

    List<OppgittArbeidsforhold> getOppgittArbeidsforhold();

    List<EgenNæring> getEgenNæring();

    List<AnnenAktivitet> getAnnenAktivitet();

    Optional<Frilans> getFrilans();
    
    /** Identifisere en immutable instans av oppgitt opptjening unikt og er egnet for utveksling (eks. til andre systemer) */
    UUID getEksternReferanse();
}
