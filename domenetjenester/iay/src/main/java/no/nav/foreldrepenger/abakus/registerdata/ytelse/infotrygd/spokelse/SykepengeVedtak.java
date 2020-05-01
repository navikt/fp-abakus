package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.spokelse;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SykepengeVedtak {

    @JsonProperty("vedtaksreferanse")
    private LocalDate vedtaksreferanse;
    @JsonProperty("utbetalinger")
    private List<SykepengeUtbetaling> utbetalinger;

    @JsonCreator
    public SykepengeVedtak(@JsonProperty("vedtaksreferanse") LocalDate vedtaksreferanse,
                           @JsonProperty("utbetalinger") List<SykepengeUtbetaling> utbetalinger) {
        this.vedtaksreferanse = vedtaksreferanse;
        this.utbetalinger = utbetalinger;
    }

    public LocalDate getVedtaksreferanse() {
        return vedtaksreferanse;
    }

    public List<SykepengeUtbetaling> getUtbetalinger() {
        return utbetalinger != null ? utbetalinger : Collections.emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SykepengeVedtak that = (SykepengeVedtak) o;
        return Objects.equals(vedtaksreferanse, that.vedtaksreferanse) &&
                Objects.equals(utbetalinger, that.utbetalinger);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vedtaksreferanse, utbetalinger);
    }

    @Override
    public String toString() {
        return "SykepengeVedtak{" +
                "vedtaksreferanse=" + vedtaksreferanse +
                ", utbetalinger=" + utbetalinger +
                '}';
    }
}
