package no.nav.foreldrepenger.abakus.lonnskomp.kafka;

import java.time.LocalDate;

public class UtbetalingsdagMelding {
    private LocalDate dato;
    private String lønnskompensasjonsbeløp;

    public LocalDate getDato() {
        return dato;
    }

    public String getLønnskompensasjonsbeløp() {
        return lønnskompensasjonsbeløp;
    }
}
