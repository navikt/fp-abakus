package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.abakus.typer.AktørId;

public interface InfotrygdGrunnlag {

    List<Grunnlag> hentGrunnlag(String fnr, LocalDate fom);

    List<Grunnlag> hentGrunnlag(String fnr, LocalDate fom, LocalDate tom);

    List<Grunnlag> hentGrunnlag(AktørId aktørId, LocalDate fom);

    List<Grunnlag> hentGrunnlag(AktørId aktørId, LocalDate fom, LocalDate tom);

}
