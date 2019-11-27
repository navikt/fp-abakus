package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.felles;

import java.util.List;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.Saker;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.InfotrygdSak;

public interface SakMapper {

    List<InfotrygdSak> map(Saker saker);

}
