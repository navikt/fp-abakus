package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.felles;

import java.time.LocalDate;
import java.util.List;


public record PersonRequest(LocalDate fom, LocalDate tom, List<String> fnr) {
}
