package no.nav.foreldrepenger.abakus.registerdata.ytelse.dpsak;

import java.time.LocalDate;
import java.util.List;

public record DagpengerRettighetsperioderDto(String personIdent, List<Rettighetsperiode> perioder) {

    public record Rettighetsperiode(LocalDate fraOgMedDato, LocalDate tilOgMedDato, DagpengerKilde kilde) {
    }

    public enum DagpengerKilde { DP_SAK, ARENA }
}

