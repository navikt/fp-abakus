package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.abakus.typer.AktørId;

public interface InfotrygdBeregningsgrunnlagTjeneste {
    List<YtelseBeregningsgrunnlag> hentGrunnlagListeFull(String fnr, LocalDate fom);

    List<YtelseBeregningsgrunnlag> hentGrunnlagListeFull(AktørId aktørId, LocalDate fom);
}
