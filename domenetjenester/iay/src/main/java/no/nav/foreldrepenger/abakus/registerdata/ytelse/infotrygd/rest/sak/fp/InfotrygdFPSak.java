package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.fp;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.FPF;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.felles.AbstractInfotrygdSak;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@FPF
public class InfotrygdFPSak extends AbstractInfotrygdSak {

    private static final String DEFAULT_URI = "http://infotrygd-foreldrepenger.default/saker";

    InfotrygdFPSak() {
        super();
    }

    @Inject
    public InfotrygdFPSak(OidcRestClient restClient,
            @KonfigVerdi(value = "fpabakus.it.fp.sak.url", defaultVerdi = DEFAULT_URI) URI uri) {
        super(restClient, uri, new InfotrygdFPSakMapper());
    }
}
