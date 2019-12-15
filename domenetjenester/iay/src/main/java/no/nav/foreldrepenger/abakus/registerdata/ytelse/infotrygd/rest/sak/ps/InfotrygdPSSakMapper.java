package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.ps;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.AvsluttetSak;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.IkkeStartetSak;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.LøpendeSak;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.Sak;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.felles.AbstractInfotrygdSakMapper;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.InfotrygdSak;

public class InfotrygdPSSakMapper extends AbstractInfotrygdSakMapper {

    @Override
    protected InfotrygdSak fraAvsluttetSak(AvsluttetSak sak) {
        throw new UnsupportedOperationException(); // for now
    }

    @Override
    protected InfotrygdSak fraSak(Sak sak) {
        throw new UnsupportedOperationException(); // for now
    }

    @Override
    protected InfotrygdSak fraLøpendeSak(LøpendeSak sak) {
        throw new UnsupportedOperationException(); // for now
    }

    @Override
    protected InfotrygdSak fraIkkeStartetSak(IkkeStartetSak sak) {
        throw new UnsupportedOperationException(); // for no
    }
}
