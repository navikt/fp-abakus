package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.felles;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.InfotrygdSak;

public interface InfotrygdSakTjeneste {

    List<InfotrygdSak> saker(String fnr, LocalDate fom);

}
