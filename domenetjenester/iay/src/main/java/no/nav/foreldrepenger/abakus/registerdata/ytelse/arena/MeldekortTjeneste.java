package no.nav.foreldrepenger.abakus.registerdata.ytelse.arena;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.abakus.typer.AktørId;

public interface MeldekortTjeneste {
    List<MeldekortUtbetalingsgrunnlagSak> hentMeldekortListe(AktørId aktørId, LocalDate fom, LocalDate tom);
}
