package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.fp;

import static no.nav.foreldrepenger.abakus.kodeverk.TemaUnderkategori.FORELDREPENGER_FODSEL;
import static no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus.AVSLUTTET;
import static no.nav.foreldrepenger.abakus.kodeverk.YtelseStatus.LØPENDE;
import static no.nav.foreldrepenger.abakus.kodeverk.YtelseType.FORELDREPENGER;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.AvsluttetSak;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.LøpendeSak;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.Sak;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.felles.AbstractInfotrygdSakMapper;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.InfotrygdSak;

public class InfotrygdFPSakMapper extends AbstractInfotrygdSakMapper {

    @Override
    protected InfotrygdSak fraAvsluttetSak(AvsluttetSak sak) {
        // TODO
        return InfotrygdSak.InfotrygdSakBuilder.ny()
                .medOpphørFom(sak.getStoppdato())
                .medIverksatt(sak.getIverksatt())
                .medYtelseType(FORELDREPENGER)
                .medTemaUnderkategori(FORELDREPENGER_FODSEL) // TODO
                .medRelatertYtelseTilstand(AVSLUTTET)
                .medPeriode(null) // TODO ??
                .medRegistrert(null) // ikke satt foreløpig
                .build();
    }

    @Override
    protected InfotrygdSak fraSak(Sak sak) {
        return InfotrygdSak.InfotrygdSakBuilder.ny()
                .medIverksatt(sak.getIverksatt())
                .medYtelseType(FORELDREPENGER)
                .medTemaUnderkategori(FORELDREPENGER_FODSEL) // TODO
                .medRelatertYtelseTilstand(null) // TODO ??
                .medPeriode(null) // TODO ??
                .medRegistrert(null) // ikke satt foreløpig
                .build();
    }

    @Override
    protected InfotrygdSak fraLøpendeSak(LøpendeSak sak) {
        return InfotrygdSak.InfotrygdSakBuilder.ny()
                .medIverksatt(sak.getIverksatt())
                .medYtelseType(FORELDREPENGER)
                .medTemaUnderkategori(FORELDREPENGER_FODSEL) // TODO
                .medRelatertYtelseTilstand(LØPENDE)
                .medPeriode(null) // TODO ??
                .medRegistrert(null) // ikke satt foreløpig
                .build();
    }

}
