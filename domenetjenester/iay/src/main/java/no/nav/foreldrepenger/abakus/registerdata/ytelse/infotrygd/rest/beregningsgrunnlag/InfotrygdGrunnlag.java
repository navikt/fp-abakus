package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag;

import java.time.LocalDate;
import java.util.List;

import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Grunnlag;

public interface InfotrygdGrunnlag {

    List<Grunnlag> hentGrunnlag(String fnr, LocalDate fom, LocalDate tom);

    List<Grunnlag> hentGrunnlagFailSoft(String fnr, LocalDate fom, LocalDate tom);

}
