package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.beregningsgrunnlag;

import java.time.LocalDate;

import no.nav.foreldrepenger.abakus.kobling.Kobling;

public interface InfotrygdBeregningsgrunnlagTjeneste {
    YtelsesBeregningsgrunnlag hentGrunnlagListeFull(Kobling behandling, String fnr, LocalDate fom);
}
