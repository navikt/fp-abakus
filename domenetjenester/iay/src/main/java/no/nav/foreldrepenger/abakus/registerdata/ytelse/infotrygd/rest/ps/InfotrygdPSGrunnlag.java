package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.ps;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.felles.AbstractInfotrygdGrunnlag;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

@ApplicationScoped
@PS
public class InfotrygdPSGrunnlag extends AbstractInfotrygdGrunnlag {

    private static final String DEFAULT_URI = "http://infotrygd-grunnlag-paaroerende-sykdom.default/paaroerendeSykdom/grunnlag";

    @Inject
    public InfotrygdPSGrunnlag(OidcRestClient restClient, @KonfigVerdi(value = "fpabakus.it.ps.grunnlag.url", defaultVerdi = DEFAULT_URI) URI uri) {
        super(restClient, uri);
    }

    public InfotrygdPSGrunnlag() {
        super();
    }
}
