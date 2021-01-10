package no.nav.foreldrepenger.abakus.lonnskomp.kafka;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class LønnskompensasjonVedtakMelding {
    private String fnr;
    private BigDecimal totalKompensasjon;
    private String bedriftNr;
    private LocalDate fom;
    private LocalDate tom;
    private String sakId;
    private LocalDate forrigeVedtakDato;
    private List<UtbetalingsdagMelding> dagBeregninger;

    public String getFnr() {
        return fnr;
    }

    public BigDecimal getTotalKompensasjon() {
        return totalKompensasjon;
    }

    public String getBedriftNr() {
        return bedriftNr;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public String getSakId() {
        return sakId;
    }

    public LocalDate getForrigeVedtakDato() {
        return forrigeVedtakDato;
    }

    public List<UtbetalingsdagMelding> getDagBeregninger() {
        return dagBeregninger;
    }
}
