package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.ps;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.FPF;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.felles.AbstractInfotrygdSak;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@FPF
public class InfotrygdPSSak extends AbstractInfotrygdSak {

    private static final String DEFAULT_URI = "http://infotrygd-grunnlag-paaroerende-sykdom.default/paaroerendeSykdom/saker";

    InfotrygdPSSak() {
        super();
    }

    @Inject
    public InfotrygdPSSak(OidcRestClient restClient,
            @KonfigVerdi(value = "fpabakus.it.ps.sak.url", defaultVerdi = DEFAULT_URI) URI uri) {
        super(restClient, uri, new InfotrygdPSSakMapper());
    }

}
