package no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OppgittOpptjening {

    List<OppgittArbeidsforhold> getOppgittArbeidsforhold();

    List<OppgittEgenNæring> getEgenNæring();

    List<OppgittAnnenAktivitet> getAnnenAktivitet();

    Optional<OppgittFrilans> getFrilans();
    
    /** Identifisere en immutable instans av oppgitt opptjening unikt og er egnet for utveksling (eks. til andre systemer) */
    UUID getEksternReferanse();
    
    /** Tidspunkt dette innslaget ble opprettet. (betyr normalt når det ble lagret i databasen her (transaction-time). */
    LocalDateTime getOpprettetTidspunkt();
    
    /** Bruker navn som originalt opprettet innslag. */
    String getOpprettetAv();

}
