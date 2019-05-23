package no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag;

import java.time.LocalDateTime;
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
    
    /** Tidspunkt dette innslaget ble opprettet. (normalt lagret i databasen her). */
    LocalDateTime getOpprettetTidspunkt();
}
