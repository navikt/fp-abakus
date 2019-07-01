package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDateTime;

public class TidssoneConfig {

    private String tidssone;
    private LocalDateTime tidsstempel;

    public TidssoneConfig(String tidssone, LocalDateTime tidsstempel) {
        this.tidssone = tidssone;
        this.tidsstempel = tidsstempel;
    }

    public String getTidssone() {
        return tidssone;
    }

    public LocalDateTime getTidsstempel() {
        return tidsstempel;
    }
}
