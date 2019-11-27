package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.svp;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.SVP;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.felles.AbstractInfotrygdSak;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@SVP
public class InfotrygdSVPSak extends AbstractInfotrygdSak {

    private static final String DEFAULT_URI = "http://infotrygd-svangerskapspenger.default/saker";

    InfotrygdSVPSak() {
        super();
    }

    @Inject
    public InfotrygdSVPSak(OidcRestClient restClient,
            @KonfigVerdi(value = "fpabakus.it.svp.sak.url", defaultVerdi = DEFAULT_URI) URI uri) {
        super(restClient, uri, new InfotrygdSVPSakMapper());
    }

}
